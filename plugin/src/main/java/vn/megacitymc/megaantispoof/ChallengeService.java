package vn.megacitymc.megaantispoof;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.*;
import com.comphenix.protocol.wrappers.nbt.*;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import vn.megacitymc.megaantispoof.api.*;
import vn.megacitymc.megaantispoof.core.*;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.nbt.*;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

final class ChallengeService implements Listener {
    private final MegaAntiSpoofPlugin plugin;
    private final VersionAdapter adapter;
    private final Messages messages;
    private final ProtocolManager protocol;
    private final ResponseClassifier classifier = new ResponseClassifier();
    private final WebhookClient webhook;
    private final ConcurrentMap<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final Set<UUID> running = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean packetEvents = new AtomicBoolean();
    private final PunishmentManager punishment;
    private PacketListener listener;

    ChallengeService(MegaAntiSpoofPlugin plugin, VersionAdapter adapter, Messages messages) {
        this.plugin = plugin; this.adapter = adapter; this.messages = messages;
        this.protocol = ProtocolLibrary.getProtocolManager(); this.webhook = new WebhookClient(plugin);
        this.punishment = new PunishmentManager(plugin, messages);
    }

    void reload() {
        punishment.load();
    }

    void enable() {
        listener = new PacketAdapter(plugin, ListenerPriority.HIGHEST, PacketType.Play.Client.UPDATE_SIGN) {
            @Override public void onPacketReceiving(PacketEvent event) {
                Session session = sessions.get(event.getPlayer().getUniqueId());
                if (session == null) return;
                BlockPosition position;
                try { position = event.getPacket().getBlockPositionModifier().read(0); }
                catch (RuntimeException ignored) { return; }
                if (!session.position.equals(position)) return;
                event.setCancelled(true);
                List<String> lines = readLines(event.getPacket());
                SchedulerFacade.later(ChallengeService.this.plugin, event.getPlayer(), 1,
                        () -> accept(event.getPlayer(), session, lines));
            }
        };
        protocol.addPacketListener(listener);
    }

    void disable() {
        if (listener != null) protocol.removePacketListener(listener);
        sessions.clear(); running.clear();
    }
    void setPacketEventsAvailable(boolean value) { packetEvents.set(value); }

    @EventHandler public void onJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("check-on-join.enabled",
                plugin.getConfig().getBoolean("kiem-tra-khi-vao.bat", true))) return;
        long delay = plugin.getConfig().getLong("check-on-join.delay-ticks",
                plugin.getConfig().getLong("kiem-tra-khi-vao.tre-tick", 60L));
        SchedulerFacade.later(plugin, event.getPlayer(), delay, () -> scan(event.getPlayer(), null));
    }
    @EventHandler public void onQuit(PlayerQuitEvent event) {
        sessions.remove(event.getPlayer().getUniqueId()); running.remove(event.getPlayer().getUniqueId());
    }

    boolean scan(Player player, Collection<String> filter) {
        if (!player.isOnline() || !running.add(player.getUniqueId())) return false;
        List<ModSignature> signatures = loadSignatures().stream()
                .filter(s -> filter == null || filter.isEmpty() || filter.contains(s.id())).toList();
        if (signatures.isEmpty()) { running.remove(player.getUniqueId()); return false; }
        Session session = new Session(signatures, clientVersion(player), extractIp(player));
        next(player, session);
        return true;
    }

    private void next(Player player, Session session) {
        if (!player.isOnline()) { finish(player, session, DetectionResult.Status.ERROR); return; }
        if (session.offset >= session.all.size()) { finish(player, session, session.detected.isEmpty() ? DetectionResult.Status.PASSED : DetectionResult.Status.FAILED); return; }
        session.current = session.all.subList(session.offset, Math.min(session.offset + adapter.maxLinesPerChallenge(), session.all.size()));
        session.offset += session.current.size();
        Location loc = player.getLocation();
        int safeY = Math.max(player.getWorld().getMinHeight(), loc.getBlockY() - 4);
        session.position = new BlockPosition(loc.getBlockX(), safeY, loc.getBlockZ());
        sessions.put(player.getUniqueId(), session);
        try { sendPackets(player, session); }
        catch (Throwable ex) {
            plugin.getLogger().warning("Không gửi được challenge cho " + player.getName() + ": " + ex.getMessage());
            finish(player, session, DetectionResult.Status.ERROR); return;
        }
        long timeout = plugin.getConfig().getLong("challenge.timeout-ticks",
                plugin.getConfig().getLong("thu-thach.het-han-tick", 40L));
        int generation = ++session.generation;
        long closeDelay = Math.max(1L, plugin.getConfig().getLong("challenge.close-gui-delay-ticks",
                plugin.getConfig().getLong("thu-thach.dong-giao-dien-sau-tick", 1L)));
        SchedulerFacade.later(plugin, player, closeDelay, () -> {
            Session active = sessions.get(player.getUniqueId());
            if (active == session && active.generation == generation) closeSignEditor(player);
        });
        SchedulerFacade.later(plugin, player, timeout, () -> {
            Session active = sessions.get(player.getUniqueId());
            if (active != session || active.generation != generation) return;
            int maxAttempts = Math.max(1, plugin.getConfig().getInt("challenge.max-retries",
                    plugin.getConfig().getInt("thu-thach.so-lan-thu-toi-da", 20)));
            if (++session.noResponseAttempts >= maxAttempts) {
                finish(player, session, DetectionResult.Status.PROTECTED);
                return;
            }
            restore(player, session.position);
            sessions.remove(player.getUniqueId(), session);
            session.offset -= session.current.size();
            SchedulerFacade.later(plugin, player,
                    plugin.getConfig().getLong("challenge.retry-interval-ticks",
                            plugin.getConfig().getLong("thu-thach.khoang-cach-thu-lai-tick", 10L)), () -> next(player, session));
        });
    }

    private void accept(Player player, Session session, List<String> lines) {
        if (sessions.get(player.getUniqueId()) != session) return;
        if (plugin.getConfig().getBoolean("debug.sign-response",
                plugin.getConfig().getBoolean("debug.phan-hoi-sign", true)))
            plugin.getLogger().info(buildResponseJson(player, session, lines));
        session.detected.addAll(classifier.detect(session.current, lines));
        session.noResponseAttempts = 0;
        restore(player, session.position);
        sessions.remove(player.getUniqueId(), session);
        long interval = plugin.getConfig().getLong("challenge.interval-ticks",
                plugin.getConfig().getLong("thu-thach.khoang-cach-tick", 3L));
        SchedulerFacade.later(plugin, player, interval, () -> next(player, session));
    }

    private void finish(Player player, Session session, DetectionResult.Status status) {
        sessions.remove(player.getUniqueId(), session); running.remove(player.getUniqueId());
        if (player.isOnline() && session.position != null) restore(player, session.position);
        List<DetectionResult.CheckedMod> checkedMods = new ArrayList<>(session.all.size());
        for (ModSignature sig : session.all) {
            checkedMods.add(new DetectionResult.CheckedMod(sig.displayName(), session.detected.contains(sig.id())));
        }
        String ip = player.isOnline() ? extractIp(player) : session.playerIp;
        if (ip == null || ip.isBlank() || ip.equals("Không xác định")) {
            ip = session.playerIp;
        }
        var result = new DetectionResult(player.getUniqueId(), player.getName(), ip, status, session.clientVersion,
                List.copyOf(session.detected), List.copyOf(session.detected), checkedMods, Instant.now());
        webhook.send(result);
        int currentViolations = punishment.getViolations(player.getUniqueId());
        if (status == DetectionResult.Status.PASSED) {
            if (punishment.shouldResetOnPass()) {
                punishment.resetViolations(player.getUniqueId());
                currentViolations = 0;
            }
        } else if (status == DetectionResult.Status.FAILED) {
            currentViolations = punishment.recordViolation(player.getUniqueId());
        }

        int maxViolations = punishment.getMaxViolations();
        String key = switch (status) { case PASSED -> "ket-qua.dat"; case FAILED -> "ket-qua.khong-dat";
            case PROTECTED -> "ket-qua.duoc-bao-ve"; default -> "ket-qua.loi"; };
        String text = messages.format(key,
                "{player}", player.getName(),
                "{mods}", String.join(", ", session.detected),
                "{violations}", String.valueOf(currentViolations),
                "{max_violations}", String.valueOf(maxViolations),
                "{discord}", punishment.getDiscordInvite());
        Bukkit.getConsoleSender().sendMessage(text);
        for (Player online : Bukkit.getOnlinePlayers()) if (online.hasPermission("megaantispoof.alert")) online.sendMessage(text);

        if (player.isOnline() && status == DetectionResult.Status.FAILED) {
            if (punishment.isBanEnabled() && currentViolations >= maxViolations) {
                punishment.executeBan(player, String.join(", ", session.detected), currentViolations);
            } else if (plugin.getConfig().getBoolean("actions.kick",
                    plugin.getConfig().getBoolean("xu-ly.kick", true))) {
                punishment.executeKick(player, String.join(", ", session.detected), currentViolations);
            }
        } else if (player.isOnline() && status == DetectionResult.Status.PROTECTED
                && plugin.getConfig().getBoolean("actions.kick-on-no-response",
                        plugin.getConfig().getBoolean("xu-ly.kick-khi-khong-phan-hoi", true))) {
            punishment.executeNoResponseKick(player);
        }
    }

    private void sendPackets(Player player, Session session) throws Exception {
        PacketContainer block = protocol.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        block.getBlockPositionModifier().write(0, session.position);
        block.getBlockData().write(0, WrappedBlockData.createData(Material.OAK_SIGN));
        protocol.sendServerPacket(player, block);

        NbtCompound root = NbtFactory.ofCompound("");
        root.put("id", "minecraft:sign");
        root.put("x", session.position.getX()); root.put("y", session.position.getY()); root.put("z", session.position.getZ());
        NbtCompound front = NbtFactory.ofCompound("front_text");
        List<String> components = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            if (i < session.current.size()) {
                ModSignature sig = session.current.get(i);
                components.add("[{\"text\":\" \"},{\"translate\":\"" + json(sig.translationKey()) + "\"}]");
            } else components.add("{\"text\":\"\"}");
        }
        front.put(NbtFactory.ofList("messages", components));
        front.put("color", "black"); front.put("has_glowing_text", (byte) 0);
        root.put(front);
        if (adapter.family().equals("26.x")) {
            sendBlockEntityWithPacketEvents(player, session, components);
        } else {
            PacketContainer tile = protocol.createPacket(PacketType.Play.Server.TILE_ENTITY_DATA);
            tile.getBlockPositionModifier().write(0, session.position);
            if (tile.getBlockEntityTypeModifier().size() == 0)
                throw new IllegalStateException("ProtocolLib không ánh xạ được BlockEntityType cho phiên bản này");
            tile.getBlockEntityTypeModifier().write(0, WrappedRegistrable.blockEntityType("minecraft:sign"));
            if (tile.getBlockEntityTypeModifier().read(0) == null)
                throw new IllegalStateException("BlockEntityType minecraft:sign bị null");
            tile.getNbtModifier().write(0, root);
            protocol.sendServerPacket(player, tile);
        }

        PacketContainer open = protocol.createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);
        open.getBlockPositionModifier().write(0, session.position);
        if (!open.getBooleans().getValues().isEmpty()) open.getBooleans().write(0, true);
        protocol.sendServerPacket(player, open);
    }

    private void sendBlockEntityWithPacketEvents(Player player, Session session, List<String> components) {
        if (!packetEvents.get()) throw new IllegalStateException("PacketEvents chưa sẵn sàng cho protocol 26.x");
        NBTCompound root = new NBTCompound();
        NBTCompound front = new NBTCompound();
        // 26.x dùng structured NBT text component. NBTString chứa JSON sẽ bị coi là literal text.
        NBTList<NBTCompound> messages = NBTList.createCompoundList();
        for (int i = 0; i < 4; i++) {
            NBTCompound line = new NBTCompound();
            if (i < session.current.size()) {
                line.setTag("text", new NBTString(" "));
                NBTList<NBTCompound> extra = NBTList.createCompoundList();
                NBTCompound translated = new NBTCompound();
                translated.setTag("translate", new NBTString(session.current.get(i).translationKey()));
                extra.addTag(translated);
                line.setTag("extra", extra);
            } else line.setTag("text", new NBTString(""));
            messages.addTag(line);
        }
        front.setTag("messages", messages);
        front.setTag("color", new NBTString("black"));
        front.setTag("has_glowing_text", new NBTByte(false));
        root.setTag("front_text", front);
        Vector3i position = new Vector3i(session.position.getX(), session.position.getY(), session.position.getZ());
        var packet = new WrapperPlayServerBlockEntityData(position, BlockEntityTypes.SIGN, root);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    private void restore(Player player, BlockPosition position) {
        try {
            Location at = new Location(player.getWorld(), position.getX(), position.getY(), position.getZ());
            PacketContainer packet = protocol.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
            packet.getBlockPositionModifier().write(0, position);
            packet.getBlockData().write(0, WrappedBlockData.createData(at.getBlock().getBlockData()));
            protocol.sendServerPacket(player, packet);
        } catch (Exception ex) { plugin.getLogger().fine("Không khôi phục fake block: " + ex.getMessage()); }
    }

    private void closeSignEditor(Player player) {
        if (!player.isOnline()) return;
        try {
            PacketContainer close = protocol.createPacket(PacketType.Play.Server.CLOSE_WINDOW);
            if (!close.getIntegers().getValues().isEmpty()) close.getIntegers().write(0, 0);
            protocol.sendServerPacket(player, close);
        } catch (Exception ex) {
            plugin.getLogger().fine("Không đóng được Sign Editor của " + player.getName() + ": " + ex.getMessage());
        }
    }

    private static List<String> readLines(PacketContainer packet) {
        try { String[] lines = packet.getStringArrays().read(0); return Arrays.asList(lines); }
        catch (RuntimeException ignored) {
            try { return List.copyOf(packet.getSpecificModifier(List.class).read(0)); }
            catch (RuntimeException ignoredAgain) { return List.of(); }
        }
    }
    private List<ModSignature> loadSignatures() {
        List<ModSignature> result = new ArrayList<>();
        ConfigurationSection root = plugin.getConfig().getConfigurationSection("blocked-mods");
        if (root == null) root = plugin.getConfig().getConfigurationSection("mods-cam");
        if (root == null) return result;
        for (String id : root.getKeys(false)) {
            String base = id + ".";
            try {
                String name = root.getString(base + "name", root.getString(base + "ten", id));
                String key = root.getString(base + "key", root.getString(base + "khoa"));
                String modeStr = root.getString(base + "mode", root.getString(base + "che-do", "TRANSLATE"));
                result.add(new ModSignature(id, name, key,
                        ModSignature.Mode.valueOf(modeStr.toUpperCase(Locale.ROOT))));
            }
            catch (RuntimeException ex) { plugin.getLogger().warning("Bỏ chữ ký mod không hợp lệ " + id + ": " + ex.getMessage()); }
        }
        return result;
    }
    private String clientVersion(Player player) {
        int protocolId = protocol.getProtocolVersion(player);
        return Bukkit.getMinecraftVersion() + " (protocol " + protocolId + (packetEvents.get() ? ", PE" : "") + ")";
    }
    private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
    private static String printable(String value) {
        return value.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n");
    }
    private static String buildResponseJson(Player player, Session session, List<String> lines) {
        StringBuilder out = new StringBuilder(256);
        out.append("SIGN-JSON {\"player\":\"").append(json(player.getName())).append("\",\"batch\":[");
        for (int i = 0; i < session.current.size(); i++) {
            if (i > 0) out.append(',');
            ModSignature signature = session.current.get(i);
            out.append("{\"mod\":\"").append(json(signature.id())).append("\",\"expected\":\"")
                    .append(json(signature.translationKey())).append("\"}");
        }
        out.append("],\"response\":[");
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) out.append(',');
            out.append('\"').append(json(lines.get(i))).append('\"');
        }
        return out.append("]}").toString();
    }

    private static String extractIp(Player player) {
        try {
            if (player != null && player.getAddress() != null && player.getAddress().getAddress() != null) {
                return player.getAddress().getAddress().getHostAddress();
            }
        } catch (Exception ignored) { }
        return "Không xác định";
    }

    private static final class Session {
        final List<ModSignature> all; final String clientVersion; final String playerIp;
        final Set<String> detected = new LinkedHashSet<>();
        volatile List<ModSignature> current = List.of(); volatile BlockPosition position;
        int offset; int generation; int noResponseAttempts;
        Session(List<ModSignature> all, String clientVersion, String playerIp) {
            this.all = List.copyOf(all);
            this.clientVersion = clientVersion;
            this.playerIp = playerIp != null ? playerIp : "Không xác định";
        }
    }
}

package github.uncandango.leakdiagtool.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import github.uncandango.leakdiagtool.LeakDiagTool;
import github.uncandango.leakdiagtool.Scheduler;
import github.uncandango.leakdiagtool.events.ClientEvents;
import github.uncandango.leakdiagtool.tracker.ClassTracker;
import github.uncandango.leakdiagtool.tracker.MxBean;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public final class LDTCommands {

    public static boolean heapDumpScheduledOnServerShutdown = false;

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("leak")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("loaded")
                                .executes(cmd -> sendObjectsReport(cmd.getSource(), ClassTracker.LOADED)))
                        .then(Commands.literal("leaking")
                                .executes(cmd -> sendObjectsReport(cmd.getSource(), ClassTracker.LEAKING)))
                        .then(Commands.literal("dump_heap")
                                .executes(cmd -> dumpHeap(cmd.getSource()))
                                .then(Commands.literal("schedule_in_minutes")
                                        .then(Commands.argument("time_in_minutes", IntegerArgumentType.integer())
                                                .executes(cmd -> scheduleHeapDump(cmd.getSource(), cmd.getArgument("time_in_minutes", Integer.class)))))
                                .then(Commands.literal("schedule_on_exit")
                                        .executes(cmd -> scheduleHeapDump(cmd.getSource(), -1)))
                        )

        );
    }

    public static void registerClientCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(
                Commands.literal("leakc")
                        .then(Commands.literal("loaded")
                                .executes(cmd -> sendObjectsReport(cmd.getSource(), ClassTracker.LOADED)))
                        .then(Commands.literal("leaking")
                                .executes(cmd -> sendObjectsReport(cmd.getSource(), ClassTracker.LEAKING)))
                        .then(Commands.literal("dump_heap")
                                .executes(cmd -> dumpHeap(cmd.getSource()))
                                .then(Commands.literal("schedule_in_minutes")
                                        .then(Commands.argument("time_in_minutes", IntegerArgumentType.integer())
                                        .executes(cmd -> scheduleHeapDump(cmd.getSource(), cmd.getArgument("time_in_minutes", Integer.class)))))
                                .then(Commands.literal("schedule_on_exit")
                                        .executes(cmd -> scheduleHeapDump(cmd.getSource(), -1)))


                        )


        );
    }

    private static int scheduleHeapDump(CommandSourceStack source, Integer minutes){
        if (minutes == -1){
            if (FMLEnvironment.dist.isClient()){
                ClientEvents.heapDumpScheduledOnClientExit = !ClientEvents.heapDumpScheduledOnClientExit;
                if (ClientEvents.heapDumpScheduledOnClientExit){
                    source.sendSystemMessage(Component.literal("Heap dump scheduled to be run on world exit for this session."));
                } else {
                    source.sendSystemMessage(Component.literal("Heap dump schedule on world exit now off."));
                }
                return 1;
            } else {
                heapDumpScheduledOnServerShutdown = !heapDumpScheduledOnServerShutdown;
                if (heapDumpScheduledOnServerShutdown){
                    source.sendSystemMessage(Component.literal("Heap dump scheduled to be run on server shutdown."));
                    source.sendSystemMessage(Component.literal("Be aware to disable any auto rescheduler/task killer from your hoster.").withStyle(ChatFormatting.GOLD));
                } else {
                    source.sendSystemMessage(Component.literal("Heap dump schedule on server shutdown now off."));
                }
                return 1;
            }
        } else {
            Scheduler.INSTANCE.schedule("heap_dump_scheduled", () -> LDTCommands.dumpHeap(Optional.of(source)), (long) minutes * 60, FMLEnvironment.dist.isClient() ? LogicalSide.CLIENT : LogicalSide.SERVER);
        }
        source.sendSystemMessage(Component.translatable("Heap dump scheduled to be run in %s minute(s)", minutes));
        return Command.SINGLE_SUCCESS;
    }

    private static int sendObjectsReport(CommandSourceStack source, ClassTracker type) {
        if (MxBean.INSTANCE.isExplicitGcDisabled()){
            var message = Component.literal("Explicit GC is disabled.\nPlease remove the following VM args: \"-XX:+DisableExplicitGC\"").withStyle(ChatFormatting.RED);
            source.sendFailure(message);
            return 0;
        }
        final var sentTitle = new AtomicBoolean(false);
        var summary = type.getSummary();
        var titleMessage = Component.literal("  Count |").withStyle(ChatFormatting.GOLD).append(" Class");
        summary.forEach((clazz, count) -> {
            var message = Component.
                    literal(String.format("%7s", count.toString())).withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(clazz.getSimpleName()).withStyle(ChatFormatting.WHITE));
            if (!sentTitle.get()) {
                source.sendSystemMessage(titleMessage);
                sentTitle.set(true);
            }
            source.sendSystemMessage(message);
        });
        if (!sentTitle.get()) source.sendSystemMessage(Component.literal(type.name() + " is empty."));
        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static boolean dumpHeap(Optional<CommandSourceStack> source) {
        LeakDiagTool.LOGGER.debug("Executing heap dump on thread: {}", Thread.currentThread().getName());
        var mxBean = MxBean.INSTANCE.get();
        var path = FMLPaths.getOrCreateGameRelativePath(Path.of("heap_dump/")).normalize().toAbsolutePath();
        var filePath = path + "/" + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + ".hprof";
        try {
            mxBean.dumpHeap(filePath, true);
            source.ifPresent(sender -> {
                var message = Component.translatable("Heap dump generated at %s", path.toString());
                sender.sendSystemMessage(message);
            });
            LeakDiagTool.LOGGER.info("Heap dump generated: {}", filePath);
            return true;
        } catch (Exception e) {
            LeakDiagTool.LOGGER.error("Something went wrong while trying to dump file: {}", e.getMessage());
        }
        return false;
    }

    public static void dumpHeap(){
        dumpHeap(Optional.empty());
    }

    private static int dumpHeap(CommandSourceStack source){
        LeakDiagTool.LOGGER.info("Running dump heap...");
        var message = Component.literal("Running dump heap in 5 seconds... this may take a few minutes on large modpacks.").withStyle(ChatFormatting.GOLD);
        source.sendSystemMessage(message);
        Scheduler.INSTANCE.schedule(
                "heap_dump_now",
                () -> LDTCommands.dumpHeap(Optional.of(source)),
                5L,
                FMLEnvironment.dist.isClient() ? LogicalSide.CLIENT : LogicalSide.SERVER
        );
        return Command.SINGLE_SUCCESS;
    }
}

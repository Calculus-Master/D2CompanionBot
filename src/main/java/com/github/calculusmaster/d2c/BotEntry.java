package com.github.calculusmaster.d2c;

import com.github.calculusmaster.basebot.registry.BaseCommandListener;
import com.github.calculusmaster.basebot.registry.BaseCommandManager;
import com.github.calculusmaster.d2c.interactions.dev.DevSlashHandler;
import com.github.calculusmaster.d2c.util.MessageListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BotEntry
{
	private static final Logger LOGGER = LoggerFactory.getLogger(BotEntry.class);

	// Global constants
	public static BaseCommandManager COMMAND_MANAGER = new BaseCommandManager();
	public static Dotenv ENV = Dotenv.load();
	public static JDA BOT = null;
	public static HttpClient HTTP_CLIENT = null;
	public static ExecutorService HANDLER_EXECUTOR = Executors.newFixedThreadPool(4);

	public static void main(String[] args) throws InterruptedException
	{
		// Create bot
		BOT = JDABuilder
				.createDefault(ENV.get("BOT_TOKEN"))
				.enableIntents(GatewayIntent.MESSAGE_CONTENT)
				.build().awaitReady();

		// Create commands
		BotEntry.registerInteractions();

		// Register commands (Guild-only for testing)
		COMMAND_MANAGER.applyGuildCommands(BOT, ENV.get("TEST_SERVER_ID"));

		// Register listeners
		BOT.addEventListener(
				new BaseCommandListener(COMMAND_MANAGER, HANDLER_EXECUTOR),
				new MessageListener()
		);

		// Create cached global HTTP client
		HTTP_CLIENT = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.followRedirects(HttpClient.Redirect.NORMAL)
				.connectTimeout(Duration.ofSeconds(10))
				.build();
	}

	public static void shutdown() throws InterruptedException
	{
		LOGGER.info("Initiating complete shutdown of the bot.");

		Duration timeout = Duration.ofSeconds(5);

		BOT.shutdown();
		if(!BOT.awaitShutdown(timeout))
		{
			BOT.shutdownNow();
			BOT.awaitShutdown();
			LOGGER.warn("Bot did not shutdown gracefully, forcing shutdown.");
		}

		HANDLER_EXECUTOR.shutdown();
		if(!HANDLER_EXECUTOR.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS))
		{
			HANDLER_EXECUTOR.shutdownNow();
			LOGGER.warn("Handler executor did not shutdown gracefully, forcing shutdown.");
		}

		HTTP_CLIENT.shutdown();
		if(!HTTP_CLIENT.awaitTermination(timeout))
		{
			HTTP_CLIENT.shutdownNow();
			LOGGER.warn("HTTP client did not shutdown gracefully, forcing shutdown.");
		}

		LOGGER.info("Bot has been shut down.");
	}

	private static void registerInteractions()
	{
		COMMAND_MANAGER.registerSlash(
				Commands.slash("dev", "Run developer commands")
						.addOption(OptionType.STRING, "dev_command", "The developer command to run", true, true)
						.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
				DevSlashHandler::new
		);
	}
}

package com.github.calculusmaster.d2c.interactions.dev;

import com.github.calculusmaster.basebot.handlers.BaseSlashHandler;
import com.github.calculusmaster.d2c.BotEntry;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Arrays;
import java.util.List;

public class DevSlashHandler extends BaseSlashHandler
{
	public DevSlashHandler(SlashCommandInteractionEvent event)
	{
		super(event);
	}

	@Override
	public boolean handle()
	{
		OptionMapping commandOption = this.event.getOption("dev_command");

		DeveloperCommand command = null;
		if(commandOption != null)
			for(DeveloperCommand c : DeveloperCommand.values())
				if(c.getName().equalsIgnoreCase(commandOption.getAsString()))
				{
					command = c;
					break;
				}

		return switch(command)
		{
			case null -> this.error("Invalid command.");
			case SHUTDOWN -> {
				InteractionHook hook = this.event
						.reply("Initiating bot shutdown...")
						.setEphemeral(true)
						.complete();

				try { BotEntry.shutdown(); }
				catch(InterruptedException e)
				{
					hook.editOriginal("Failed to shutdown bot properly.").queue();
					e.printStackTrace();
					yield false;
				}

				yield true;
			}
		};
	}

	@Override
	public void autocomplete(CommandAutoCompleteInteractionEvent event)
	{
		String input = event.getFocusedOption().getValue();
		List<String> choices = Arrays.stream(DeveloperCommand.values())
				.map(DeveloperCommand::getName)
				.filter(n -> n.toLowerCase().contains(input.toLowerCase()))
				.toList();

		event.replyChoiceStrings(choices).queue();
	}
}

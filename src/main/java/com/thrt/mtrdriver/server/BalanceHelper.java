package com.thrt.mtrdriver.server;

import org.mtr.mapping.holder.Scoreboard;
import org.mtr.mapping.holder.ScoreboardCriterionRenderType;
import org.mtr.mapping.holder.ScoreboardObjective;
import org.mtr.mapping.holder.Text;
import org.mtr.mapping.holder.World;
import org.mtr.mapping.mapper.ScoreboardCriteria;
import org.mtr.mapping.mapper.ScoreboardHelper;
import org.mtr.mapping.mapper.TextHelper;
import org.mtr.mod.data.TicketSystem;

public final class BalanceHelper {
	private BalanceHelper() {
	}

	public static void addBalance(World world, String playerName, int amount) {
		if (world == null || playerName == null || playerName.isEmpty() || amount <= 0) {
			return;
		}

		final Scoreboard scoreboard = world.getScoreboard();
		ScoreboardObjective objective = ScoreboardHelper.getScoreboardObjective(scoreboard, TicketSystem.BALANCE_OBJECTIVE);
		if (objective == null) {
			try {
				objective = ScoreboardHelper.addObjective(scoreboard, TicketSystem.BALANCE_OBJECTIVE, ScoreboardCriteria.DUMMY, Text.cast(TextHelper.literal(TicketSystem.BALANCE_OBJECTIVE_TITLE)), ScoreboardCriterionRenderType.INTEGER);
			} catch (Exception ignored) {
				objective = ScoreboardHelper.getScoreboardObjective(scoreboard, TicketSystem.BALANCE_OBJECTIVE);
			}
		}

		if (objective != null) {
			ScoreboardHelper.incrementPlayerScore(scoreboard, playerName, objective, amount);
		}
	}
}

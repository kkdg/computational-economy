/*
Copyright (C) 2013 u.wol@wwu.de 
 
This file is part of ComputationalEconomy.

ComputationalEconomy is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ComputationalEconomy is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with ComputationalEconomy. If not, see <http://www.gnu.org/licenses/>.
 */

package compecon.economy.behaviour.impl;

import compecon.economy.agent.Agent;
import compecon.economy.behaviour.BudgetingBehaviour;
import compecon.economy.sectors.financial.Currency;
import compecon.engine.applicationcontext.ApplicationContext;
import compecon.engine.log.Log;

/**
 * This behaviour controls buying decisions.It is injected into an agent (thus
 * compositions instead of inheritance). <br />
 * The key interest rate influences the buying behaviour via a simulated
 * transmission mechanism.
 */
public class BudgetingBehaviourImpl implements BudgetingBehaviour {

	protected final Agent agent;

	protected double lastMaxCreditRate = Double.NaN;

	public BudgetingBehaviourImpl(Agent agent) {
		this.agent = agent;
	}

	/*
	 * maxCredit defines the maximum additional debt the buyer is going to
	 * accept -> nexus with the monetary sphere
	 */
	public double calculateTransmissionBasedBudgetForPeriod(
			final Currency currency, final double bankAccountBalance,
			final double referenceCredit) {

		/*
		 * set / adjust reference credit
		 */

		if (Double.isNaN(this.lastMaxCreditRate)) {
			this.lastMaxCreditRate = referenceCredit;
		}

		final double keyInterestRate = ApplicationContext.getInstance()
				.getAgentService().findCentralBank(currency)
				.getEffectiveKeyInterestRate();

		assert (!Double.isNaN(keyInterestRate));

		final double internalRateOfReturn = ApplicationContext.getInstance()
				.getConfiguration().budgetingBehaviourConfig
				.getInternalRateOfReturn();
		final double keyInterestRateTransmissionDamper = ApplicationContext
				.getInstance().getConfiguration().budgetingBehaviourConfig
				.getKeyInterestRateTransmissionDamper();
		lastMaxCreditRate = lastMaxCreditRate
				* (1.0 + ((internalRateOfReturn - keyInterestRate) / keyInterestRateTransmissionDamper));

		/*
		 * transmission mechanism
		 */

		final double creditBasedBudget = Math.max(0.0, bankAccountBalance
				+ this.lastMaxCreditRate);

		if (getLog().isAgentSelectedByClient(BudgetingBehaviourImpl.this.agent))
			getLog().log(
					BudgetingBehaviourImpl.this.agent,
					Currency.formatMoneySum(creditBasedBudget) + " "
							+ currency.getIso4217Code() + " budget = ("
							+ Currency.formatMoneySum(bankAccountBalance) + " "
							+ currency.getIso4217Code()
							+ " bankAccountBalance + "
							+ Currency.formatMoneySum(lastMaxCreditRate) + " "
							+ currency.getIso4217Code() + " maxCredit)");
		return creditBasedBudget;
	}

	public double getCreditBasedBudgetCapacity() {
		return lastMaxCreditRate;
	}

	private Log getLog() {
		return ApplicationContext.getInstance().getLog();
	}
}

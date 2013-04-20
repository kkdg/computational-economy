/*
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

package compecon.culture.sectors.state;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;

import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.engine.Agent;
import compecon.engine.Log;
import compecon.engine.dao.HibernateDAOFactory;
import compecon.engine.time.ITimeSystemEvent;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.MonthType;

@Entity
@Table(name = "State")
public class State extends Agent {

	@Enumerated(EnumType.STRING)
	protected Currency legislatedCurrency;

	@Override
	public void initialize() {
		super.initialize();

		// balance sheet publication
		ITimeSystemEvent balanceSheetPublicationEvent = new BalanceSheetPublicationEvent();
		this.timeSystemEvents.add(balanceSheetPublicationEvent);
		compecon.engine.time.TimeSystem.getInstance().addEvent(
				balanceSheetPublicationEvent, -1, MonthType.EVERY,
				DayType.EVERY, BALANCE_SHEET_PUBLICATION_HOUR_TYPE);
	}

	/*
	 * Accessors
	 */

	public Currency getLegislatedCurrency() {
		return this.legislatedCurrency;
	}

	public void setLegislatedCurrency(final Currency legislatedCurrency) {
		this.legislatedCurrency = legislatedCurrency;
	}

	/*
	 * Business logic
	 */

	@Transient
	public void doDeficitSpending() {
		this.assertTransactionsBankAccount();
		for (CreditBank creditBank : HibernateDAOFactory.getCreditBankDAO()
				.findAll()) {
			for (Agent agent : creditBank.getCustomers()) {
				if (agent != this) {
					this.primaryBank.transferMoney(transactionsBankAccount,
							agent.getTransactionsBankAccount(), 100,
							this.bankPasswords.get(this.primaryBank),
							"deficit spending");
				}
			}
		}
	}

	protected class BalanceSheetPublicationEvent implements ITimeSystemEvent {
		@Override
		public void onEvent() {
			State.this.assertTransactionsBankAccount();
			Log.agent_onPublishBalanceSheet(State.this,
					State.this.issueBasicBalanceSheet());
		}
	}
}

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

package compecon;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import compecon.culture.sectors.financial.Currency;
import compecon.engine.AgentFactory;
import compecon.engine.jmx.Agents;
import compecon.engine.time.TimeSystem;
import compecon.engine.time.calendar.DayType;
import compecon.engine.time.calendar.HourType;
import compecon.engine.time.calendar.MonthType;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;

public class Simulation {

	protected static int millisecondsToSleepPerHourType = 0;

	protected static boolean paused = false;

	protected static boolean singleStep = false;

	public static void setMillisecondsToSleepPerHourType(
			int millisecondsToSleepPerHourType) {
		Simulation.millisecondsToSleepPerHourType = millisecondsToSleepPerHourType;
	}

	public static void setPaused(boolean paused) {
		Simulation.paused = paused;
	}

	public static void setSingleStep() {
		Simulation.singleStep = true;
	}

	public static void main(String[] args) {
		try {
			// init database connection
			HibernateUtil.openSession();

			// init JMX
			MBeanServer mBeanServer = ManagementFactory
					.getPlatformMBeanServer();
			ObjectName objectName = new ObjectName("compecon.jmx:type=Agents");
			Agents agentsMBean = new Agents();
			mBeanServer.registerMBean(agentsMBean, objectName);

			// configure simulation
			final int NUMBER_OF_CREDITBANKSPERCURRENCY = 2;
			final int NUMBER_OF_HOUSEHOLDS = 500;
			final int NUMBER_OF_FACTORIES_PER_GOODTYPE = 5;

			// initialize the time system, so that agents can register their
			// events
			TimeSystem timeSystem = TimeSystem.getInstance(2000,
					MonthType.JANUARY, DayType.DAY_01);

			// initialize states for currencies
			for (Currency currency : Currency.values()) {
				AgentFactory.getInstanceState(currency);
			}

			// initialize central banks for currencies
			for (Currency currency : Currency.values()) {
				AgentFactory.getInstanceCentralBank(currency);
			}

			// initialize credit banks
			for (Currency currency : Currency.values()) {
				Set<Currency> offeredCurrencies = new HashSet<Currency>();
				offeredCurrencies.add(currency);

				for (int i = 0; i < NUMBER_OF_CREDITBANKSPERCURRENCY; i++) {
					AgentFactory.newInstanceCreditBank(offeredCurrencies);
				}
			}

			// initialize factories
			for (GoodType goodType : GoodType.values()) {
				if (!GoodType.LABOURHOUR.equals(goodType)) {
					for (int i = 0; i < NUMBER_OF_FACTORIES_PER_GOODTYPE; i++) {
						AgentFactory.newInstanceFactory(goodType);
					}
				}
			}

			// initialize households
			for (int i = 0; i < NUMBER_OF_HOUSEHOLDS; i++) {
				AgentFactory.newInstanceHousehold();
			}

			HibernateUtil.flushSession();

			// start simulation
			while (true) {
				if (!paused) {
					// step hour-wise; trigger events in time system
					timeSystem.nextHour();
					Thread.sleep(millisecondsToSleepPerHourType);
				} else {
					if (singleStep) {
						timeSystem.nextHour();
						if (HourType.HOUR_00.equals(TimeSystem.getInstance()
								.getCurrentHourType()))
							singleStep = false;
					} else
						Thread.sleep(50);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			HibernateUtil.closeSession();
		}
	}
}

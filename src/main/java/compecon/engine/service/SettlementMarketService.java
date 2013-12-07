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

package compecon.engine.service;

import compecon.economy.markets.MarketParticipant;
import compecon.economy.materia.GoodType;
import compecon.economy.property.Property;
import compecon.economy.sectors.financial.BankAccountDelegate;
import compecon.economy.sectors.financial.Currency;

public interface SettlementMarketService extends MarketService {

	public void placeSettlementSellingOffer(final GoodType goodType,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount, final double pricePerUnit);

	public void placeSettlementSellingOffer(
			final Currency commodityCurrency,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double amount,
			final double pricePerUnit,
			final BankAccountDelegate commodityCurrencyOfferorsBankAcountDelegate);

	public void placeSettlementSellingOffer(final Property property,
			final MarketParticipant offeror,
			final BankAccountDelegate offerorsBankAcountDelegate,
			final double pricePerUnit);

	/**
	 * @return total price and total amount
	 */
	public double[] buy(final GoodType goodType, final double maxAmount,
			final double maxTotalPrice, final double maxPricePerUnit,
			final MarketParticipant buyer,
			final BankAccountDelegate buyersBankAccountDelegate);

	/**
	 * @return total price and total amount
	 */
	public double[] buy(
			final Currency commodityCurrency,
			final double maxAmount,
			final double maxTotalPrice,
			final double maxPricePerUnit,
			final MarketParticipant buyer,
			final BankAccountDelegate buyersBankAccountDelegate,
			final BankAccountDelegate buyersBankAccountForCommodityCurrencyDelegate);

	/**
	 * @return total price and total amount
	 */
	public double[] buy(final Class<? extends Property> propertyClass,
			final double maxAmount, final double maxTotalPrice,
			final double maxPricePerUnit, final MarketParticipant buyer,
			final BankAccountDelegate buyersBankAccountDelegate);

	public void removeAllSellingOffers(final MarketParticipant offeror);
}

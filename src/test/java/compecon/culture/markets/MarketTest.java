package compecon.culture.markets;

import static org.junit.Assert.assertEquals;

import java.util.SortedMap;

import org.junit.Test;

import compecon.culture.sectors.financial.CentralBank;
import compecon.culture.sectors.financial.CreditBank;
import compecon.culture.sectors.financial.Currency;
import compecon.culture.sectors.household.Household;
import compecon.culture.sectors.industry.Factory;
import compecon.culture.sectors.state.law.property.PropertyRegister;
import compecon.engine.AgentFactory;
import compecon.engine.MarketFactory;
import compecon.engine.dao.DAOFactory;
import compecon.engine.util.HibernateUtil;
import compecon.nature.materia.GoodType;

public class MarketTest {
	@Test
	public void offerGoodTypeAndDeleteOffer() {

		double epsilon = 0.0001;

		Currency currency = Currency.EURO;
		GoodType goodType = GoodType.LABOURHOUR;

		// init database connection
		HibernateUtil.openSession();

		CentralBank centralBank = AgentFactory.getInstanceCentralBank(currency);
		centralBank.assertTransactionsBankAccount();
		CreditBank creditBank = AgentFactory.newInstanceCreditBank(currency);
		creditBank.assertCentralBankAccount();
		creditBank.assertTransactionsBankAccount();
		Household household1 = AgentFactory.newInstanceHousehold();
		household1.assertTransactionsBankAccount();
		Household household2 = AgentFactory.newInstanceHousehold();
		household2.assertTransactionsBankAccount();

		Factory factory1 = AgentFactory
				.newInstanceFactory(GoodType.MEGACALORIE);
		factory1.assertTransactionsBankAccount();

		assertEquals(
				DAOFactory.getGoodTypeMarketOfferDAO().findMarginalPrice(
						currency, goodType), Double.NaN, epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType, household1,
				household1.getTransactionsBankAccount(), 10, 5, currency);
		MarketFactory.getInstance().placeSellingOffer(goodType, household2,
				household2.getTransactionsBankAccount(), 10, 4, currency);
		assertEquals(
				DAOFactory.getGoodTypeMarketOfferDAO().findMarginalPrice(
						currency, goodType), 4.0, epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(household2);
		assertEquals(
				DAOFactory.getGoodTypeMarketOfferDAO().findMarginalPrice(
						currency, goodType), 5.0, epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType, household2,
				household2.getTransactionsBankAccount(), 10, 3, currency);
		assertEquals(
				DAOFactory.getGoodTypeMarketOfferDAO().findMarginalPrice(
						currency, goodType), 3.0, epsilon);

		MarketFactory.getInstance().removeAllSellingOffers(household2,
				currency, goodType);
		assertEquals(
				DAOFactory.getGoodTypeMarketOfferDAO().findMarginalPrice(
						currency, goodType), 5.0, epsilon);

		MarketFactory.getInstance().placeSellingOffer(goodType, household2,
				household2.getTransactionsBankAccount(), 10, 3, currency);
		assertEquals(
				DAOFactory.getGoodTypeMarketOfferDAO().findMarginalPrice(
						currency, goodType), 3.0, epsilon);

		SortedMap<GoodTypeMarketOffer, Double> marketOffers1 = MarketFactory
				.getInstance().findBestFulfillmentSet(goodType, currency, 20,
						-1, 3);
		assertEquals(marketOffers1.size(), 1);

		SortedMap<GoodTypeMarketOffer, Double> marketOffers2 = MarketFactory
				.getInstance().findBestFulfillmentSet(goodType, currency, 20,
						-1, 5);
		assertEquals(marketOffers2.size(), 2);

		MarketFactory.getInstance().buy(goodType, currency, 5, -1, 8, factory1,
				factory1.getTransactionsBankAccount(),
				factory1.getBankPasswords().get(factory1.getPrimaryBank()));
		assertEquals(
				PropertyRegister.getInstance().getBalance(factory1, goodType),
				5, epsilon);

		// close database conenction
		HibernateUtil.closeSession();
	}
}

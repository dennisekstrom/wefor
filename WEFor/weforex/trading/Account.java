package trading;

import java.util.Currency;
import java.util.Set;

import com.dukascopy.api.IAccount;

public class Account implements IAccount {

	private Currency currency;

	@Override
	public Currency getCurrency() {
		return currency;
	}

	@Override
	public String getAccountId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getBalance() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Set<String> getClientIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getCreditLine() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getEquity() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getLeverage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMarginCutLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getOverWeekEndLeverage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getUseOfLeverage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isGlobal() {
		// TODO Auto-generated method stub
		return false;
	}
}
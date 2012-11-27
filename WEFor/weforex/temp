package trading;

import com.dukascopy.api.IEngine;

import forex.ForexException;

public final class OrderHandler {

	private IEngine engine;

	public void registerEngine(IEngine engine) {
		if (this.engine != null)
			throw new ForexException(
					"only one engine might be registered with an OrderHandler.");
		if (engine == null)
			throw new ForexException("engine can't be null");

		this.engine = engine;
	}

	// TODO submitOrder methods

}
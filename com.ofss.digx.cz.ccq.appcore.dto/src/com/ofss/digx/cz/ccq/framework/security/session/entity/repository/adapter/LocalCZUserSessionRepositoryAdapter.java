package com.ofss.digx.cz.ccq.framework.security.session.entity.repository.adapter;

import com.ofss.digx.cz.ccq.framework.security.session.entity.CZUserSession;
import com.ofss.digx.cz.ccq.framework.security.session.entity.CZUserSessionKey;
import com.ofss.digx.framework.determinant.DeterminantResolver;
import com.ofss.digx.framework.domain.repository.adapter.AbstractLocalRepositoryAdapter;
import com.ofss.digx.infra.exceptions.Exception;

public class LocalCZUserSessionRepositoryAdapter extends 
AbstractLocalRepositoryAdapter<CZUserSession>
implements ICZUserSessionRepositoryAdapter  {
	/**
	 * Singleton instance of the repository
	 */
	private static LocalCZUserSessionRepositoryAdapter singletonInstance;

	/**
	 * Private Constructor of the Repository
	 */
	private LocalCZUserSessionRepositoryAdapter() {

	}

	/**
	 * This method returns unique instance of CZUserSessionRepository
	 * 
	 * @return singletonInstance
	 */
	public static LocalCZUserSessionRepositoryAdapter getInstance() {
		if (singletonInstance == null) {
			synchronized (LocalCZUserSessionRepositoryAdapter.class) {
				if (singletonInstance == null) {
					singletonInstance = new LocalCZUserSessionRepositoryAdapter();
				}
			}
		}
		return singletonInstance;
	}

	@Override
	public void create(CZUserSession object) throws Exception {
		super.create(object);

	}

	@Override
	public void delete(CZUserSession var1) throws Exception {
		super.delete(var1);

	}

	@Override
	public CZUserSession read(CZUserSessionKey var1) throws Exception {
		
		return super.load(CZUserSession.class, var1);
	}

	@Override
	public void update(CZUserSession var1) throws Exception {
		super.update(var1);

	}

}

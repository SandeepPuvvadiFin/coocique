package com.ofss.digx.cz.ccq.framework.security.session.entity.repository;

import com.ofss.digx.cz.ccq.framework.security.session.entity.CZUserSession;
import com.ofss.digx.cz.ccq.framework.security.session.entity.CZUserSessionKey;
import com.ofss.digx.cz.ccq.framework.security.session.entity.repository.adapter.ICZUserSessionRepositoryAdapter;
import com.ofss.digx.cz.ccq.framework.security.session.entity.repository.adapter.LocalCZUserSessionRepositoryAdapter;
import com.ofss.digx.framework.domain.repository.AbstractDomainObjectRepository;
import com.ofss.digx.infra.exceptions.Exception;

public class CZUserSessionRepository extends AbstractDomainObjectRepository<CZUserSession, CZUserSessionKey> {
	/**
	 * Singleton instance of the repository
	 */
	private static CZUserSessionRepository singletonInstance;

	/**
	 * Private Constructor of the Repository
	 */
	private CZUserSessionRepository() {

	}

	/**
	 * This method returns unique instance of CZUserSessionRepository
	 * 
	 * @return singletonInstance
	 */
	public static CZUserSessionRepository getInstance() {
		if (singletonInstance == null) {
			synchronized (CZUserSessionRepository.class) {
				if (singletonInstance == null) {
					singletonInstance = new CZUserSessionRepository();
				}
			}
		}
		return singletonInstance;
	}

	@Override
	public void create(CZUserSession var1) throws Exception {
		ICZUserSessionRepositoryAdapter adapter= LocalCZUserSessionRepositoryAdapter.getInstance();
		adapter.create(var1);

	}

	@Override
	public void delete(CZUserSession var1) throws Exception {
		ICZUserSessionRepositoryAdapter adapter= LocalCZUserSessionRepositoryAdapter.getInstance();
		adapter.delete(var1);

	}

	@Override
	public CZUserSession read(CZUserSessionKey var1) throws Exception {
		ICZUserSessionRepositoryAdapter adapter= LocalCZUserSessionRepositoryAdapter.getInstance();
		return adapter.read(var1);
	}

	@Override
	public void update(CZUserSession var1) throws Exception {
		ICZUserSessionRepositoryAdapter adapter= LocalCZUserSessionRepositoryAdapter.getInstance();
		adapter.update(var1);

	}

}

package eu.cactosfp7.cactoopt.cyclicoptimiser;

import java.util.logging.Logger;

import org.eclipse.emf.cdo.net4j.CDONet4jSession;
import org.eclipse.emf.cdo.view.CDOView;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationPlan;

/**
 * The context of an optimisation.
 * @author jakub, stier
 *
 */
public class OptimisationContext {
//	CDONet4jSession cdoSession;
	CactosCdoSession cdoSession;
	LogicalDCModel ldcm;
	PhysicalDCModel pdcm;
	LogicalLoadModel llm;
	PhysicalLoadModel plm;
	OptimisationPlan plan;
	boolean retry;
	
	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(CyclicOptimiser.class.getName());
	
//	OptimisationContext(CDONet4jSession cdoSession, LogicalDCModel ldcm, PhysicalDCModel pdcm, LogicalLoadModel llm, PhysicalLoadModel plm, OptimisationPlan plan) {
	OptimisationContext(CactosCdoSession cdoSession, LogicalDCModel ldcm, PhysicalDCModel pdcm, LogicalLoadModel llm, PhysicalLoadModel plm, OptimisationPlan plan) {
		this.cdoSession = cdoSession;
		this.ldcm = ldcm;
		this.pdcm = pdcm;
		this.llm = llm;
		this.plm = plm;
		this.plan = plan;
	}
	
	OptimisationContext(CactosCdoSession cdoSession, LogicalDCModel ldcm, PhysicalDCModel pdcm, LogicalLoadModel llm, PhysicalLoadModel plm, OptimisationPlan plan, boolean retry) {
		this.cdoSession = cdoSession;
		this.ldcm = ldcm;
		this.pdcm = pdcm;
		this.llm = llm;
		this.plm = plm;
		this.plan = plan;
		this.retry = retry;
	}
	
	/**
	 * Closes all open transactions
	 */
//	void closeAll() {
//		for(CDOView view : cdoSession.getViews()) {
//			log.info("closing " + view.toString());
//			view.close();
//		}
//		
//		if(cdoSession.getTransactions().length > 0 ||
//				cdoSession.getViews().length > 0) {
//			log.severe("open views and/or transactions.");
//		}
////		cdoSession.close();			
//	}
}

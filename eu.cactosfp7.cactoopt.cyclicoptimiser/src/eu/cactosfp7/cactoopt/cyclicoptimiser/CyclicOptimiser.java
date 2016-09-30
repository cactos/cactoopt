package eu.cactosfp7.cactoopt.cyclicoptimiser;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.util.ConcurrentAccessException;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.spi.cdo.CDOMergingConflictResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import eu.cactosfp7.cactoopt.optimisationservice.IOptimisationAlgorithm;
import eu.cactosfp7.cactoopt.optimisationservice.autoscaling.AutoScalingOptimisationService;
import eu.cactosfp7.cactoopt.optimisationservice.registry.OptimisationSettings;
import eu.cactosfp7.cactoopt.optimisationservice.resourcecontrol.ResourceControlOptimisationService;
import eu.cactosfp7.cactoopt.util.CDOOptimisationPlanHandler;
import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationStep;
import eu.cactosfp7.optimisationplan.ParallelSteps;
import eu.cactosfp7.optimisationplan.SequentialSteps;

/**
 * A cyclic optimiser task that can be used to periodically trigger
 * optimisations.
 * 
 * @author jakub, stier
 *
 */
public class CyclicOptimiser implements Runnable {
	private IOptimisationAlgorithm optimisationService;
	private CactosCdoSession cactosCdoSession;

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(CyclicOptimiser.class.getName());

	/**
	 * Creates a cyclic optimiser.
	 * 
	 * @param optimisationService
	 *            The optimisation service to which <code>this</code> issues its
	 *            optimisation calls.
	 * @param cactosCdoSession
	 *            The session used to interact with the Runtime Model
	 *            Repository.
	 * @param ivmiService
	 *            The VMI Controller component used to enact the optimisations.
	 */
	public CyclicOptimiser(IOptimisationAlgorithm optimisationService, CactosCdoSession cactosCdoSession) {
		this.optimisationService = optimisationService;
		this.cactosCdoSession = cactosCdoSession;
	}

	@Override
	public void run() {
		try {
			log.info("Next iteration of cycylic optimiser started.");
	        CDOTransaction transaction = this.cactosCdoSession.createTransaction();
	        transaction.options().addConflictResolver(new CDOMergingConflictResolver());
			OptimisationContext ctx = optimise(transaction, this.optimisationService);

			// if optimisation decided to not do any actions, stop here!
			if (ctx.plan == null)
				return;

			String planId;

			if (ctx.retry) {
				planId = ctx.plan.getId();
				log.info("Restarting optimisation plan [" + planId + "]");
			} else {
				// SequentialSteps rootStep = (SequentialSteps)
				// ctx.plan.getOptimisationStep();
				OptimisationStep rootStep = ctx.plan.getOptimisationStep();

				if (rootStep == null) {
					log.info("Optimisation root step is null!");
					planId = null;
				} else {
					int numberOfOptimisationActions = -1;

					if (rootStep instanceof SequentialSteps) {
						log.info("Optimisation root step is sequential!");
						SequentialSteps sequentialRootStep = (SequentialSteps) rootStep;
						if (sequentialRootStep.getOptimisationSteps() == null) {
							log.info("Optimisation root doesn't contain any child steps!");
							planId = null;
						} else {
							numberOfOptimisationActions = sequentialRootStep.getOptimisationSteps().size();
						}
					} else if (rootStep instanceof ParallelSteps) {
						log.info("Optimisation root step is parallel!");
						ParallelSteps parallelRootStep = (ParallelSteps) rootStep;
						if (parallelRootStep.getOptimisationSteps() == null) {
							log.info("Optimisation root doesn't contain any child steps!");
							planId = null;
						} else {
							numberOfOptimisationActions = parallelRootStep.getOptimisationSteps().size();
						}
					} else {
						log.severe("Optimisation root step has unknown type!");
					}

					if (numberOfOptimisationActions > 0) {
						// ctx.plan.setId(UUID.randomUUID().toString());
						planId = ctx.plan.getId();
						CDOOptimisationPlanHandler.saveOptimisationPlan(ctx.plan, transaction,
								this.cactosCdoSession.getOptimisationPlanPath());
						transaction.commit();
						transaction.close();
						log.info("New OptimisationPlan saved to repository.");
					} else {
						log.info("Nothing to optimise.");
						planId = null;
					}
				}
			}
			
			// ctx.closeAll();
			
			return;
		} catch (Throwable t) {
			log.log(Level.SEVERE, "Exception while executing optimisation in CyclicOptimiser.", t);
		}
	}

	/**
	 * Issued to trigger a single optimisation cycle.
	 * 
	 * @param optimisationService
	 * @return
	 */
	private OptimisationContext optimise(CDOTransaction transaction, IOptimisationAlgorithm optimisationService) {

		// CDONet4jSession cdoSession = this.cactosCdoSession.getCdoSession();
		// CactosCdoSession cactosCdoSession =
		// CdoSessionClient.INSTANCE.getService()
		// .getCactosCdoSession(CactosUser.CACTOSCALE);
		cactosCdoSession = CdoSessionClient.INSTANCE.getService().getCactosCdoSession(CactosUser.CACTOOPT);
		log.info("CDO session [" + cactosCdoSession.toString() + "] opened.");

		// for (CDOView view : cdoSession.getViews()) {
		// log.info(view.toString() + " still open!");
		// }
		//
		// for (CDOTransaction transactions : cdoSession.getTransactions()) {
		// log.info(transactions.toString() + " still open!");
		// }
		log.info("CDO view [" + transaction.toString() + "] opened.");

		LogicalDCModel ldcm = null;
		PhysicalDCModel pdcm = null;
		LogicalLoadModel llm = null;
		PhysicalLoadModel plm = null;
		OptimisationPlan plan = null;
		// check if all plans are completed
//		boolean finished = CDOOptimisationPlanHandler.allPlansFinished(view,
//				this.cactosCdoSession.getOptimisationPlanPath());
		boolean finished = CDOOptimisationPlanHandler.allPlansFinished(this.cactosCdoSession);
		boolean retry = false;

		// callAutoscaler("Reg", 10, 2, 5, "app_1");
		// String type, int server_speed, int capacity, int load_request, String
		// app_id

		if (finished) {
			ldcm = CDOOptimisationPlanHandler.loadLogicalDc(transaction, this.cactosCdoSession.getLogicalModelPath());
			llm = CDOOptimisationPlanHandler.loadLogicalLoad(transaction, this.cactosCdoSession.getLogicalLoadPath());

			pdcm = CDOOptimisationPlanHandler.loadPhysicalDc(transaction, this.cactosCdoSession.getPhysicalModelPath());
			plm = CDOOptimisationPlanHandler.loadPhysicalLoad(transaction, this.cactosCdoSession.getPhysicalLoadPath());

			if ((ldcm != null) && (pdcm != null) && (llm != null) && (plm != null)) {
				// optimisation of virtual machine placement (migrations)
				plan = optimisationService.generateOptimizationPlan(pdcm, ldcm, plm, llm);

				// autoscaling of existing applications with white box models
				BundleContext bundleContext = Activator.getContext();
				String filter = "(&(objectclass=" + IOptimisationAlgorithm.class.getName()
						+ ")(optimisationName=AutoScaling))";
				ServiceReference<?>[] autoScalerReferences = null;
				try {
					autoScalerReferences = bundleContext.getServiceReferences(IOptimisationAlgorithm.class.getName(),
							filter);
				} catch (InvalidSyntaxException e) {
					e.printStackTrace();
				}
				if ((autoScalerReferences != null) && (autoScalerReferences.length == 1)) {
					AutoScalingOptimisationService autoScaler = (AutoScalingOptimisationService) bundleContext
							.getService(autoScalerReferences[0]);
					OptimisationPlan autoScalingPlan = autoScaler.generateOptimizationPlan(pdcm, ldcm, plm, llm);
					if (autoScalingPlan != null)
						plan = CDOOptimisationPlanHandler.mergeParallelOptimisationPlans(plan, autoScalingPlan);
					else
						log.severe("Autoscaler plan is null!");
				} else {
					log.severe("Autoscaler reference not found!");
				}
				
				if (OptimisationSettings.RESOURCE_CONTROL_ENABLED) {
					// resource control of existing applications with white box models
					String filterResourceControl = "(&(objectclass=" + IOptimisationAlgorithm.class.getName()
							+ ")(optimisationName=ResourceControl))";
					ServiceReference<?>[] resourceControlReferences = null;
					try {
						resourceControlReferences = bundleContext.getServiceReferences(IOptimisationAlgorithm.class.getName(),
								filterResourceControl);
					} catch (InvalidSyntaxException e) {
						e.printStackTrace();
					}
					if ((resourceControlReferences != null) && (resourceControlReferences.length == 1)) {
						ResourceControlOptimisationService resourceController = (ResourceControlOptimisationService) bundleContext
								.getService(resourceControlReferences[0]);
						OptimisationPlan resourceControlPlan = resourceController.generateOptimizationPlan(pdcm, ldcm, plm, llm);
						if (resourceControlPlan != null)
							plan = CDOOptimisationPlanHandler.mergeParallelOptimisationPlans(plan, resourceControlPlan);
						else
							log.severe("Resource Control plan is null!");
					} else {
						log.severe("Resource Control reference not found!");
					}
				}
			} else {
				log.severe("Infrastructure models are not complete!");
			}
		} else {
			plan = CDOOptimisationPlanHandler.getFirstReadyOptimisatioPlan(transaction, this.cactosCdoSession.getOptimisationPlanPath());
//			plan = CDOOptimisationPlanHandler.getFirstReadyOptimisatioPlan(this.cactosCdoSession);
			
			if (plan != null) {
				retry = true;
				log.info("CactoOpt will restart optimisation plan [" + plan.getId() + "]");
			} else
				log.info("No optimisation plan produced or restarted!");
		}

		// return new OptimisationContext(cdoSession, ldcm, pdcm, llm, plm, plan);
		return new OptimisationContext(cactosCdoSession, ldcm, pdcm, llm, plm, plan, retry);
	}
}

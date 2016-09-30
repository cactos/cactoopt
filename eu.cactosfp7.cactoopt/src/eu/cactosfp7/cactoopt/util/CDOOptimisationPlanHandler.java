/**
 * 
 */
package eu.cactosfp7.cactoopt.util;

import java.util.Date;
import java.util.logging.Logger;

import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.spi.cdo.CDOMergingConflictResolver;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.infrastructuremodels.load.logical.LogicalLoadModel;
import eu.cactosfp7.infrastructuremodels.load.physical.PhysicalLoadModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.physicaldc.core.PhysicalDCModel;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationPlanRepository;
import eu.cactosfp7.optimisationplan.OptimisationStep;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.OptimisationplanPackage;
import eu.cactosfp7.optimisationplan.ParallelSteps;
import eu.cactosfp7.optimisationplan.ScaleOut;
import eu.cactosfp7.optimisationplan.SequentialSteps;

/**Convenience functions to access OptimisationPlans in a shared CDO repository.
 * 
 * @author hgroenda, jakub
 *
 */
public class CDOOptimisationPlanHandler {

	/** The logger for this class. */
	private static final Logger log = Logger.getLogger(CDOOptimisationPlanHandler.class.getName());

	/**
	 * Loads physical DC model from CDO repository
	 * @param session CDO session
	 * @param resourcePath Path to the resource
	 * @return Physical DC model
	 */
	public static PhysicalDCModel loadPhysicalDc(CDOView view, String resourcePath) {
		PhysicalDCModel model = null;
		
		try {
			eu.cactosfp7.infrastructuremodels.physicaldc.core.impl.CorePackageImpl.eINSTANCE.getClass();
			
//			CDOView view = session.openView();
			CDOResource resource = view.getResource(resourcePath);
			
//			printPhysicalDCModel((PhysicalDCModel)resource.getContents().get(0));
			model = (PhysicalDCModel)resource.getContents().get(0);
//			view.close();			
			log.info("Loading physical DC model from CDO repo successful.");
		} catch (Exception e) {
			log.warning("Loading physical DC model from CDO repo failed.");
//			e.printStackTrace();
		}
		
		return model;
	}
	
	/**
	 * Loads logical DC model from CDO repository
	 * @param session CDO session
	 * @param resourcePath Path to the resource
	 * @return Logical DC model
	 */
	public static LogicalDCModel loadLogicalDc(CDOView view, String resourcePath) {
		LogicalDCModel model = null;
		
		try {
			eu.cactosfp7.infrastructuremodels.logicaldc.core.impl.CorePackageImpl.eINSTANCE.getClass();
			
//			CDOView view = session.openView();
			CDOResource resource = view.getResource(resourcePath);
			
//			printLogicalDCModel((LogicalDCModel)resource.getContents().get(0));
			model = (LogicalDCModel)resource.getContents().get(0);
//			view.close();
			log.info("Loading logical DC model from CDO repo successful.");
		} catch (Exception e) {
			log.warning("Loading logical DC model from CDO repo failed.");
//			e.printStackTrace();
		}

		return model;
	}
	
	/**
	 * Loads physical load model from CDO repository
	 * @param session CDO session
	 * @param resourcePath Path to the resource
	 * @return Physical load model
	 */
	public static PhysicalLoadModel loadPhysicalLoad(CDOView view, String resourcePath) {
		PhysicalLoadModel model = null;
		
		try {
			eu.cactosfp7.infrastructuremodels.load.physical.impl.PhysicalPackageImpl.eINSTANCE.getClass();
			
//			CDOView view = session.openView();
			CDOResource resource = view.getResource(resourcePath);
			
//			printPhysicalDCModel((PhysicalDCModel)resource.getContents().get(0));
			model = (PhysicalLoadModel)resource.getContents().get(0);
//			view.close();			
			log.info("Loading physical DC model from CDO repo successful.");
		} catch (Exception e) {
			log.warning("Loading physical DC model from CDO repo failed.");
//			e.printStackTrace();
		}
		
		return model;
	}

	/**
	 * Loads logical load model from CDO repository
	 * @param session CDO session
	 * @param resourcePath Path to the resource
	 * @return Logical load model
	 */
	public static LogicalLoadModel loadLogicalLoad(CDOView view, String resourcePath) {
		LogicalLoadModel model = null;
		
		try {
			eu.cactosfp7.infrastructuremodels.load.logical.impl.LogicalPackageImpl.eINSTANCE.getClass();
			
//			CDOView view = session.openView();
			CDOResource resource = view.getResource(resourcePath);
			
//			printLogicalDCModel((LogicalDCModel)resource.getContents().get(0));
			model = (LogicalLoadModel)resource.getContents().get(0);
//			view.close();			
			log.info("Loading logical load model from CDO repo successful.");
		} catch (Exception e) {
			log.warning("Loading logical load model from CDO repo failed.");
//			e.printStackTrace();
		}
		
		return model;
	}
	
	/**
	 * Checks if all plans stored in CDO repository are finished
	 * @param session CDO session
	 * @param resourcePath Path to the resource
	 * @return True is all Optimisation Plans are finished, False if the is at least one Optimisation Plan with status IN_EXECUTION or READY
	 */
//	public static boolean allPlansFinished(CDOTransaction transaction, String resourcePath) {
	public static boolean allPlansFinished(CactosCdoSession cactosCdoSession) {
		OptimisationPlanRepository planRepo = null;
		
		CDOTransaction transaction = null;
		CDOView view = null;
		
		try {
			OptimisationplanPackage.eINSTANCE.getClass();
			
			String resourcePath = cactosCdoSession.getOptimisationPlanPath();
			
			transaction = cactosCdoSession.createTransaction();
			log.info("CDO transaction [" + transaction.toString() + "] opened.");
			
			log.info("Adding " + resourcePath + " resource (if it doesn't exist).");
			CDOResource resource = transaction.getOrCreateResource(resourcePath);
			
			EList<EObject> contentsPlansResource = resource.getContents();
			if (contentsPlansResource.size() == 0) {
				log.info("Adding OptimisationPlanRepository object to " + resourcePath + " resource.");
				
//				transaction = cactosCdoSession.createTransaction();
				contentsPlansResource.add(OptimisationplanFactory.eINSTANCE.createOptimisationPlanRepository());
			} else {
				log.info("OptimisationPlanRepository object already exists in " + resourcePath + " resource.");
			}
			cactosCdoSession.commitAndCloseConnection(transaction);
			log.info("CDO transaction [" + transaction.toString() + "] committed and closed.");
			
			view = cactosCdoSession.createView();
			CDOResource resourceView = view.getResource(resourcePath);
			planRepo = (OptimisationPlanRepository)resourceView.getContents().get(0);
			
			EList<OptimisationPlan> plans = planRepo.getOptimisationPlans();
			log.info("Loading optimisation plans from CDO repo successful.");

			if (planRepo.getOptimisationPlans().isEmpty()) {
				log.info("No Optimisation Plans in repository.");
			}
			
			// check all optimisation plans in repository
			for (OptimisationPlan plan : planRepo.getOptimisationPlans()) {
				// if any of the stored optimisation plans has status IN_EXECUTION or READY, next optimisation iteration shouldn't be run
				ExecutionStatus status = plan.getExecutionStatus();
				if ((status == ExecutionStatus.IN_EXECUTION) || (status == ExecutionStatus.READY)) {
					log.warning("Optimisation plan: " + plan.getId() + " has status " + status.getName() 
							+ ". CactoOpt will wait for all plans to be processed.");
					return false;
				} else {
					log.info("Optimisation plan: " + plan.getId() + " has status " + status.getName() + ".");
				}
			}
			// if there is no optimisation plan with status IN_EXECUTION or READ, return that all are finished 
			log.info("All optimisation plans were processed. CactoOpt will prepare a now plan.");
				
			cactosCdoSession.closeConnection(view);
			log.info("CDO view [" + view.toString() + "] closed.");
			return true;
			
		} catch (Exception e) {
			if (transaction != null) {
				cactosCdoSession.closeConnection(transaction);
				log.warning("CDO transaction [" + transaction.toString() + "] closed without commit!");
			}
			if (view != null) {
				cactosCdoSession.closeConnection(view);
				log.info("CDO view [" + view.toString() + "] closed.");
			}
			log.warning("Loading optimisation plans from CDO repo failed.");
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean optimisatioPlanAlreadyExists(CDOView view, String resourcePath, String planId) {
		OptimisationPlanRepository planRepo = null;
		
		try {
			OptimisationplanPackage.eINSTANCE.getClass();
			
			CDOResource resource = view.getResource(resourcePath);
			
			planRepo = (OptimisationPlanRepository)resource.getContents().get(0);
			
			EList<OptimisationPlan> plans = planRepo.getOptimisationPlans();
			log.info("Loading optimisation plans from CDO repo successful.");

			if (planRepo.getOptimisationPlans().isEmpty()) {
				log.info("No Optimisation Plans in repository.");
			}
			
			// check all optimisation plans in repository
			for (OptimisationPlan plan : planRepo.getOptimisationPlans()) {
				if (plan.getId().startsWith(planId)) {
					log.info("Optimisation plan: " + plan.getId() + " exists and has status " + plan.getExecutionStatus().getName() + ".");
					return true;
				}
			}
			// if there is no optimisation plan with status IN_EXECUTION or READ, return that all are finished 
			log.info("No optimisation plans with id [" + planId + "]");
				
			return false;
			
		} catch (Exception e) {
			log.warning("Loading optimisation plans from CDO repo failed.");
			e.printStackTrace();
			return false;
		}
	}
	
	public static OptimisationPlan getFirstReadyOptimisatioPlan(CactosCdoSession cactosCdoSession) {
//	public static OptimisationPlan getFirstReadyOptimisatioPlan(CDOView view, String resourcePath) {
//		OptimisationPlanRepository planRepo = null;
//		
//		try {
//			OptimisationplanPackage.eINSTANCE.getClass();
//			
//			log.info("Adding " + resourcePath + " resource (if it doesn't exist).");
////			CDOResource resource = transaction.getOrCreateResource(resourcePath);
//			CDOResource resource = view.getResource(resourcePath);
////			transaction.commit();
		OptimisationPlanRepository planRepo = null;
		
		CDOTransaction transaction = null;
		CDOView view = null;
		
		try {
			OptimisationplanPackage.eINSTANCE.getClass();
			
			String resourcePath = cactosCdoSession.getOptimisationPlanPath();
			
			transaction = cactosCdoSession.createTransaction();
			log.info("CDO transaction [" + transaction.toString() + "] opened.");
			
			log.info("Adding " + resourcePath + " resource (if it doesn't exist).");
			CDOResource resource = transaction.getOrCreateResource(resourcePath);
			
			EList<EObject> contentsPlansResource = resource.getContents();
			if (contentsPlansResource.size() == 0) {
				log.info("Adding OptimisationPlanRepository object to " + resourcePath + " resource.");
				
//				transaction = cactosCdoSession.createTransaction();
				contentsPlansResource.add(OptimisationplanFactory.eINSTANCE.createOptimisationPlanRepository());
			} else {
				log.info("OptimisationPlanRepository object already exists in " + resourcePath + " resource.");
			}
			cactosCdoSession.commitAndCloseConnection(transaction);
			log.info("CDO transaction [" + transaction.toString() + "] committed and closed.");
			
			view = cactosCdoSession.createView();
			CDOResource resourceView = view.getResource(resourcePath);
			planRepo = (OptimisationPlanRepository)resourceView.getContents().get(0);
			
			planRepo = (OptimisationPlanRepository)resource.getContents().get(0);
			
			EList<OptimisationPlan> plans = planRepo.getOptimisationPlans();
			log.info("Loading optimisation plans from CDO repo successful.");

			if (planRepo.getOptimisationPlans().isEmpty()) {
				log.info("No Optimisation Plans in repository.");
			}
			
			// check all optimisation plans in repository
			for (OptimisationPlan plan : planRepo.getOptimisationPlans()) {
				// if any of the stored optimisation plans has status IN_EXECUTION or READY, next optimisation iteration shouldn't be run
				ExecutionStatus status = plan.getExecutionStatus();
				if (status == ExecutionStatus.READY) {
					log.info("Optimisation plan: " + plan.getId() + " has status " + status.getName() 
							+ ". CactoOpt will restart it!");
					
					OptimisationPlan newPlan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
					newPlan.setId(plan.getId());
					
//					OptimisationStep newRootStep = null;
//					if (plan.getOptimisationStep() instanceof SequentialSteps) {
//						log.info("Optimisation root step is sequential!");
//						newRootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
//					} else if (plan.getOptimisationStep() instanceof ParallelSteps) {
//						log.info("Optimisation root step is parallel!");
//						newRootStep = OptimisationplanFactory.eINSTANCE.createParallelSteps();
//					} else {
//						log.severe("Optimisation root step has unknown type!");
//					}

//					newPlan.setOptimisationStep(newRootStep);
//					newRootStep.setOptimisationPlan(newPlan);
					newPlan.setExecutionStatus(ExecutionStatus.READY);
					newPlan.setCreationDate(plan.getCreationDate());
					
					return newPlan;
				} else {
					log.info("Optimisation plan: " + plan.getId() + " has status " + status.getName() + ".");
				}
			}
			// if there is no optimisation plan with status IN_EXECUTION or READ, return that all are finished 
			log.info("No optimisation plans in state READY.");
				
			return null;
			
		} catch (Exception e) {
			log.warning("Loading optimisation plans from CDO repo failed.");
			e.printStackTrace();
			return null;
		}
	}
	
	public static OptimisationPlan getFirstReadyOptimisatioPlan(CDOView view, String resourcePath) {
		OptimisationPlanRepository planRepo = null;
		
		try {
			
			CDOResource resourceView = view.getResource(resourcePath);
			planRepo = (OptimisationPlanRepository)resourceView.getContents().get(0);
			
			EList<OptimisationPlan> plans = planRepo.getOptimisationPlans();
			log.info("Loading optimisation plans from CDO repo successful.");

			if (planRepo.getOptimisationPlans().isEmpty()) {
				log.info("No Optimisation Plans in repository.");
			}
			
			// check all optimisation plans in repository
			for (OptimisationPlan plan : planRepo.getOptimisationPlans()) {
				// if any of the stored optimisation plans has status IN_EXECUTION or READY, next optimisation iteration shouldn't be run
				ExecutionStatus status = plan.getExecutionStatus();
				if (status == ExecutionStatus.READY) {
					log.info("Optimisation plan: " + plan.getId() + " has status " + status.getName() 
							+ ". CactoOpt will restart it!");
					
					OptimisationPlan newPlan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
					newPlan.setId(plan.getId());
					
//					OptimisationStep newRootStep = null;
//					if (plan.getOptimisationStep() instanceof SequentialSteps) {
//						log.info("Optimisation root step is sequential!");
//						newRootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
//					} else if (plan.getOptimisationStep() instanceof ParallelSteps) {
//						log.info("Optimisation root step is parallel!");
//						newRootStep = OptimisationplanFactory.eINSTANCE.createParallelSteps();
//					} else {
//						log.severe("Optimisation root step has unknown type!");
//					}

//					newPlan.setOptimisationStep(newRootStep);
//					newRootStep.setOptimisationPlan(newPlan);
					newPlan.setExecutionStatus(ExecutionStatus.READY);
					newPlan.setCreationDate(plan.getCreationDate());
					
					return newPlan;
				} else {
					log.info("Optimisation plan: " + plan.getId() + " has status " + status.getName() + ".");
				}
			}
			// if there is no optimisation plan with status IN_EXECUTION or READ, return that all are finished 
			log.info("No optimisation plans in state READY.");
				
			return null;
			
		} catch (Exception e) {
			log.warning("Loading optimisation plans from CDO repo failed.");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Checks if all plans stored in CDO repository are finished
	 * @param session CDO session
	 * @param resourcePath Path to the resource
	 * @return True is all Optimisation Plans are finished, False if the is at least one Optimisation Plan with status IN_EXECUTION or READY
	 */
//	public static boolean allPlansFinishedPlacement(CDOView view, String resourcePath) {
	public static boolean allPlansFinishedPlacement(CactosCdoSession cactosCdoSession) {	
		OptimisationPlanRepository planRepo = null;
		CDOTransaction transaction = null;
		CDOView view = null;
		
		try {
			OptimisationplanPackage.eINSTANCE.getClass();
			
			String resourcePath = cactosCdoSession.getOptimisationPlanPath();
			
			transaction = cactosCdoSession.createTransaction();
			log.info("CDO transaction [" + transaction.toString() + "] opened.");
			
			log.info("Adding " + resourcePath + " resource (if it doesn't exist).");
			CDOResource resource = transaction.getOrCreateResource(resourcePath);
			
			EList<EObject> contentsPlansResource = resource.getContents();
			if (contentsPlansResource.size() == 0) {
				log.info("Adding OptimisationPlanRepository object to " + resourcePath + " resource.");
				
//				contentsPlansResource.add(eu.cactosfp7.optimisationplan.OptimisationPlanRepository.class.newInstance());
				contentsPlansResource.add(OptimisationplanFactory.eINSTANCE.createOptimisationPlanRepository());
			} else {
				log.info("OptimisationPlanRepository object already exists in " + resourcePath + " resource.");
			}
			cactosCdoSession.commitAndCloseConnection(transaction);
			log.info("CDO transaction [" + transaction.toString() + "] committed and closed.");
			
			view = cactosCdoSession.createView();
			CDOResource resourceView = view.getResource(resourcePath);
			
			planRepo = (OptimisationPlanRepository)resourceView.getContents().get(0);
			
			EList<OptimisationPlan> plans = planRepo.getOptimisationPlans();
			log.info("Loading optimisation plans from CDO repo successful.");

			if (planRepo.getOptimisationPlans().isEmpty()) {
				log.info("No Optimisation Plans in repository.");
			}
			
			// check all optimisation plans in repository
			boolean planReadyOrInExecutionWithoutScaleOut = false;
			
			for (OptimisationPlan plan : planRepo.getOptimisationPlans()) {
				// if any of the stored optimisation plans has status IN_EXECUTION or READY, next optimisation iteration shouldn't be run
				ExecutionStatus status = plan.getExecutionStatus();
				if (status == ExecutionStatus.READY)
					planReadyOrInExecutionWithoutScaleOut = true;
				if (status == ExecutionStatus.IN_EXECUTION) {
					OptimisationStep rootStep = plan.getOptimisationStep();
					
					if (rootStep instanceof SequentialSteps) {
						log.info("Root step of plan to add is sequential!");
						SequentialSteps sequentialRootStep = (SequentialSteps) plan.getOptimisationStep();
						boolean sequentialRootStepContainsScaleOut = containsScaleOut(sequentialRootStep.getOptimisationSteps());
						
						if (sequentialRootStepContainsScaleOut)
							return true;
					} else if (rootStep instanceof ParallelSteps) {
						log.info("Root step of plan to add is parallel!");
						ParallelSteps parallelRootStep = (ParallelSteps) plan.getOptimisationStep();
						boolean parallelRootStepContainsScaleOut = containsScaleOut(parallelRootStep.getOptimisationSteps());
						
						if (parallelRootStepContainsScaleOut)
							return true;
					} else {
						log.severe("Root step of plan to add has unknown type!");
						return false;
					}
					
//					EList<OptimisationStep> optimisationSteps = null;
//					if (rootStep instanceof SequentialSteps) {
//						log.info("Root step of plan to add is sequential!");
//						SequentialSteps sequentialRootStep = (SequentialSteps) plan.getOptimisationStep();
//						optimisationSteps = sequentialRootStep.getOptimisationSteps();
//					} else if (rootStep instanceof ParallelSteps) {
//						log.info("Root step of plan to add is parallel!");
//						ParallelSteps parallelRootStep = (ParallelSteps) plan.getOptimisationStep();
//						optimisationSteps = parallelRootStep.getOptimisationSteps();
//					} else {
//						log.severe("Root step of plan to add has unknown type!");
//						return false;
//					}
//					
//					for (OptimisationStep step : optimisationSteps)
//					{
//						if (step instanceof ScaleOut) {
//							ScaleOut scaleOutAction = (ScaleOut) step;
//							log.info("Optimisation plan: " + plan.getId() + " has status " + status.getName() 
//							+ " and contains ScaleOut action [" + scaleOutAction.getScalingConnector().getId()
//							+ ", " + scaleOutAction.getLoadBalancerInstance() + "].");
//							return true;
//						}
//					}
					
					planReadyOrInExecutionWithoutScaleOut = true;
				}
				log.warning("Optimisation plan: " + plan.getId() + " has status " + status.getName() + ".");
			}
			
			cactosCdoSession.closeConnection(view);
			log.info("CDO view [" + view.toString() + "] closed.");

			if (planReadyOrInExecutionWithoutScaleOut) {
				log.warning("CactoOpt will wait for all plans to be processed.");
				return false;
			} else {
				log.info("All optimisation plans were processed. CactoOpt will prepare a now plan.");
				return true;
			}
		} catch (Exception e) {
			if (transaction != null) {
				cactosCdoSession.closeConnection(transaction);
				log.warning("CDO transaction [" + transaction.toString() + "] closed without commit!");
			}
			if (view != null) {
				cactosCdoSession.closeConnection(view);
				log.info("CDO view [" + view.toString() + "] closed.");
			}
			
			log.warning("Loading optimisation plans from CDO repo failed.");
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean containsScaleOut(EList<OptimisationStep> optimisationSteps) {
		for (OptimisationStep step : optimisationSteps)
		{
			if (step instanceof ScaleOut) {
				ScaleOut scaleOutAction = (ScaleOut) step;
				log.info("ScaleOut action [" + scaleOutAction.getScalingConnector().getId()
				+ ", " + scaleOutAction.getLoadBalancerInstance() + "].");
				return true;
			}
			if (step instanceof ParallelSteps) {
				log.info("Nested Parallel Step [" + step.getId() + "]");
				boolean nestedParallelSteps = containsScaleOut(((ParallelSteps) step).getOptimisationSteps());
				
				if (nestedParallelSteps)
					return true;
			}
			if (step instanceof SequentialSteps) {
				log.info("Nested Sequential Step [" + step.getId() + "]");
				boolean nestedSequentialSteps = containsScaleOut(((SequentialSteps) step).getOptimisationSteps());
				
				if (nestedSequentialSteps)
					return true;
			}
		}
		
		return false;
	}
	
	   /**
     * Saves Optimisation Plan
     * @param plan Optimisation Plan to save
     * @param transaction2 CDO session
     * @param path Path to the resource
     */
//    public static void saveOptimisationPlan(OptimisationPlan plan, CDONet4jSession session, String path) {
	public static void saveOptimisationPlan(OptimisationPlan plan, CDOTransaction transaction, String path) {;
        final CDOResource resource = transaction.getResource(path);
        OptimisationPlanRepository planRepo = (OptimisationPlanRepository) resource.getContents().get(0);
        
        planRepo.getOptimisationPlans().add(plan);
        
        log.info("Saving optimisation actions to CDO repo successful.");
    }
    
    public static OptimisationPlan mergeParallelOptimisationPlans(OptimisationPlan planToExtend, OptimisationPlan planToAdd) {
    	
    	if ((planToExtend == null) && (planToAdd == null)) {
    		log.warning("Both plan to extend and plan to add are null. Return null.");
    		return null;
    	}
    	
    	if ((planToExtend != null) && (planToAdd == null)) {
    		log.warning("Plan to add is null. Return plan to extend.");
    		return planToExtend;
    	}
    	
    	if ((planToExtend == null) && (planToAdd != null)) {
    		log.warning("Plan to extend is null. Return plan to add.");
    		return planToAdd;
    	}
    	
    	OptimisationStep rootStepOfPlanToExtend = planToExtend.getOptimisationStep();
    	OptimisationStep rootStepOfPlanToAdd = planToAdd.getOptimisationStep();
    	
    	if (rootStepOfPlanToAdd == null) {
    		log.warning("Plan to add is empty. Return plan to extend.");
//    		log.info(rootStepOfPlanToExtend.getOptimisationSteps().size() + " actions in the returned optimisation plan");
    		return planToExtend;
    	}
    	
    	int numberOfOptimisationActionsInPlanToAdd = -1;
    	
    	if (rootStepOfPlanToAdd instanceof SequentialSteps) {
			log.info("Root step of plan to add is sequential!");
			SequentialSteps sequentialRootStep = (SequentialSteps) rootStepOfPlanToAdd;
			if (sequentialRootStep.getOptimisationSteps() == null) {
				log.info("Optimisation root doesn't contain any child steps!");
			} else {
				numberOfOptimisationActionsInPlanToAdd = sequentialRootStep.getOptimisationSteps().size();
				log.info("Optimisation root contains " + numberOfOptimisationActionsInPlanToAdd + " steps!");
			}
		} else if (rootStepOfPlanToAdd instanceof ParallelSteps) {
			log.info("Root step of plan to add is parallel!");
			ParallelSteps parallelRootStep = (ParallelSteps) rootStepOfPlanToAdd;
			if (parallelRootStep.getOptimisationSteps() == null) {
				log.info("Optimisation root doesn't contain any child steps!");
			} else {
				numberOfOptimisationActionsInPlanToAdd = parallelRootStep.getOptimisationSteps().size();
				log.info("Optimisation root contains " + numberOfOptimisationActionsInPlanToAdd + " steps!");
			}
		} else {
			log.severe("Root step of plan to add has unknown type!");
		}
    		
    	if (rootStepOfPlanToExtend == null) { // && rootStepOfPlanToAdd != null based on the previous condition
    		log.warning("Plan to extend is empty. Return plan to add.");
//    		log.info(rootStepOfPlanToAdd.getOptimisationSteps().size() + " actions in the returned optimisation plan");
    		return planToAdd;
    	}
    	
    	int numberOfOptimisationActionsInPlanToExtend = -1;
    	
    	if (rootStepOfPlanToExtend instanceof SequentialSteps) {
			log.info("Root step of plan to extend is sequential!");
			SequentialSteps sequentialRootStep = (SequentialSteps) rootStepOfPlanToExtend;
			if (sequentialRootStep.getOptimisationSteps() == null) {
				log.info("Optimisation root doesn't contain any child steps!");
			} else {
				numberOfOptimisationActionsInPlanToExtend = sequentialRootStep.getOptimisationSteps().size();
				log.info("Optimisation root contains " + numberOfOptimisationActionsInPlanToExtend + " steps!");
			}
		} else if (rootStepOfPlanToExtend instanceof ParallelSteps) {
			log.info("Root step of plan to extend is parallel!");
			ParallelSteps parallelRootStep = (ParallelSteps) rootStepOfPlanToExtend;
			if (parallelRootStep.getOptimisationSteps() == null) {
				log.info("Optimisation root doesn't contain any child steps!");
			} else {
				numberOfOptimisationActionsInPlanToExtend = parallelRootStep.getOptimisationSteps().size();
				log.info("Optimisation root contains " + numberOfOptimisationActionsInPlanToExtend + " steps!");
			}
		} else {
			log.severe("Root step of plan to extend has unknown type!");
		}
    	
		if (planToExtend.getOptimisationStep() == null) {
			log.info("Plan doesn't contain any steps.");
			rootStepOfPlanToExtend = OptimisationplanFactory.eINSTANCE.createParallelSteps();
			planToExtend.setOptimisationStep(rootStepOfPlanToExtend);
			rootStepOfPlanToExtend.setOptimisationPlan(planToExtend);
			rootStepOfPlanToExtend.setExecutionStatus(ExecutionStatus.READY);
			planToExtend.setCreationDate(new Date());
			log.info("Root step added.");
		}

		if (numberOfOptimisationActionsInPlanToAdd < 1) {
			return planToExtend;
		}
		
		if (numberOfOptimisationActionsInPlanToExtend < 1) {
			return planToAdd;
		}
		
		if ((numberOfOptimisationActionsInPlanToExtend > 0) 
				&& (numberOfOptimisationActionsInPlanToAdd > 0)){
			ParallelSteps newRootStepOfPlanToExtend = OptimisationplanFactory.eINSTANCE.createParallelSteps();
			planToExtend.setOptimisationStep(newRootStepOfPlanToExtend);
			rootStepOfPlanToExtend.setSequentialSteps(null);
			rootStepOfPlanToExtend.setParallelSteps(newRootStepOfPlanToExtend);
			rootStepOfPlanToAdd.setSequentialSteps(null);
			rootStepOfPlanToAdd.setParallelSteps(newRootStepOfPlanToExtend);
		}
		
//    	log.info(rootStepOfPlanToExtend.getOptimisationSteps().size() + " actions in the optimisation plan to extend");
//    	log.info(rootStepOfPlanToAdd.getOptimisationSteps().size() + " actions in the optimisation plan to add");
    	
//    	int noOfOptimisationActionInPlan = rootStepOfPlanToAdd.getOptimisationSteps().size();
    	
//    	for(int i = 0; i < noOfOptimisationActionInPlan; i++) {    		
//    		rootStepOfPlanToAdd.getOptimisationSteps().get(0).setSequentialSteps(rootStepOfPlanToExtend);
//    	}
    	
//    	log.info(rootStepOfPlanToExtend.getOptimisationSteps().size() + " actions in the extended optimisation plan");
    	
    	return planToExtend;
    }
    
    
    public static OptimisationPlan mergeOptimisationPlans(OptimisationPlan planToExtend, OptimisationPlan planToAdd) {
    	
    	if ((planToExtend == null) && (planToAdd == null)) {
    		log.warning("Both plan to extend and plan to add are null. Return null.");
    		return null;
    	}
    	
    	if ((planToExtend != null) && (planToAdd == null)) {
    		log.warning("Plan to add is null. Return plan to extend.");
    		return planToExtend;
    	}
    	
    	if ((planToExtend == null) && (planToAdd != null)) {
    		log.warning("Plan to extend is null. Return plan to add.");
    		return planToAdd;
    	}
    	
    	SequentialSteps rootStepOfPlanToExtend = (SequentialSteps) planToExtend.getOptimisationStep();
    	SequentialSteps rootStepOfPlanToAdd = (SequentialSteps) planToAdd.getOptimisationStep();
    	
    	if ((rootStepOfPlanToAdd == null) || (rootStepOfPlanToAdd.getOptimisationSteps() == null)) {
    		log.warning("Plan to add is empty. Return plan to extend.");
    		log.info(rootStepOfPlanToExtend.getOptimisationSteps().size() + " actions in the returned optimisation plan");
    		return planToExtend;
    	}
    		
    	if ((rootStepOfPlanToExtend == null) || (rootStepOfPlanToExtend.getOptimisationSteps() == null)
    			 || (rootStepOfPlanToExtend.getOptimisationSteps().size() == 0)) {
    		log.warning("Plan to extend is empty. Return plan to add.");
    		log.info(rootStepOfPlanToAdd.getOptimisationSteps().size() + " actions in the returned optimisation plan");
    		return planToAdd;
    	}
    	
		if (planToExtend.getOptimisationStep() == null) {
			log.info("Plan doesn't contain any steps.");
			rootStepOfPlanToExtend = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
			planToExtend.setOptimisationStep(rootStepOfPlanToExtend);
			rootStepOfPlanToExtend.setOptimisationPlan(planToExtend);
			rootStepOfPlanToExtend.setExecutionStatus(ExecutionStatus.READY);
			planToExtend.setCreationDate(new Date());
			log.info("Root step added.");
		}
    	
    	log.info(rootStepOfPlanToExtend.getOptimisationSteps().size() + " actions in the optimisation plan to extend");
    	log.info(rootStepOfPlanToAdd.getOptimisationSteps().size() + " actions in the optimisation plan to add");
    	
    	int noOfOptimisationActionInPlan = rootStepOfPlanToAdd.getOptimisationSteps().size();
    	
    	for(int i = 0; i < noOfOptimisationActionInPlan; i++) {    		
    		rootStepOfPlanToAdd.getOptimisationSteps().get(0).setSequentialSteps(rootStepOfPlanToExtend);
    	}
    	
    	log.info(rootStepOfPlanToExtend.getOptimisationSteps().size() + " actions in the extended optimisation plan");
    	
    	return planToExtend;
    }
}
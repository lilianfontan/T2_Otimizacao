package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import metaheuristics.grasp.AbstractGRASP;
import problems.Evaluator;
import problems.qbf.QBF_Inverse;
import solutions.Solution;

public class QBF_SC extends AbstractGRASP<Integer> {

	public QBF_SC(Evaluator<Integer> objFunction, Double alpha, Integer iterations) {
		super(objFunction, alpha, iterations);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ArrayList<Integer> makeCL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Integer> makeRCL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateCL() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Solution<Integer> createEmptySol() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Solution<Integer> localSearch() {
		// TODO Auto-generated method stub
		return null;
	}

}

package MainExample;

import Scheduler.Action;
import Scheduler.Joiner;

import java.util.List;
import java.util.Map;

public class Join extends Joiner<Integer, Integer, Integer> {
	public Join(String name, long progress, Map<String, List<Action<?, ?>>> actions) {
		super(name, progress, actions);
		setParallelPreviousInput(false);
	}

	@Override
	protected Integer join(Map<String, Integer> elements) {

		double sum = 0;
		for (Integer element : elements.values()) {
			sum += element;
		}

		// Aggregates outputs with the average
		return (int) sum / elements.size();
	}

	// Processes the input of each parallel execution
	@Override
	protected Integer input(String name, Integer element) {

		if (name.contains("1")) {
			element += 5;
		} else if (name.contains("2")) {
			element -= 5;
		}

		return element;
	}

	// Processes the input of Joiner execution
	@Override
	protected Integer infoProcessing(Object info) {
		return info == null ? 0 : (Integer) info;
	}
}

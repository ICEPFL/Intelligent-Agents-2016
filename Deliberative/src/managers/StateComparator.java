package managers;

import java.util.Comparator;
import java.util.LinkedList;

public class StateComparator implements Comparator<State> {

	@Override
	public int compare(State left, State right) {
		if (left.getCost() < right.getCost())
			return -1;

		if (left.getCost() == right.getCost())
			return 0;

		return 1;
	}

	public int partitionState(LinkedList<State> queue, int left, int right) {
		int i = left;
		int j = right;
		double pivot = queue.get((left + right) / 2).getCost();

		while (i <= j) {
			while (queue.get(i).getCost() < pivot)
				i += 1;
			while (queue.get(j).getCost() > pivot)
				j -= 1;

			if (i <= j) {
				State temp = queue.get(i);
				queue.set(i, queue.get(j));
				queue.set(j, temp);
				i += 1;
				j -= 1;
			}
		}

		return i;
	}

	public void quickSortState(LinkedList<State> queue, int left, int right) {
		int index = this.partitionState(queue, left, right);
		if (left < index - 1)
			quickSortState(queue, left, index - 1);
		if (index < right)
			quickSortState(queue, index, right);

	}

}

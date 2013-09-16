package feed;

import java.util.ArrayList;
import java.util.Collection;

import util.Interval;

/**
 * Intervals are ordered by comparing start and end times. Overlapping intervals
 * are merged together.
 * 
 * Start and stop times of contained intervals are both inclusive.
 * 
 * TODO fixa balanserat
 */
public class IntervalTreeSet {

	private class Node {
		private Node left, right;
		private Interval intv;

		public Node(Interval interval) {
			if (interval == null)
				throw new IllegalArgumentException("Interval must not be null.");

			this.intv = interval;
		}

		/**
		 * Returns all intervals contained by this <code>IntervalTreeSet</code>.
		 * 
		 * @return All intervals contained by this <code>IntervalTreeSet</code>.
		 */
		public ArrayList<Interval> getIntervals() {
			ArrayList<Interval> list;
			if (left == null && right == null) {
				list = new ArrayList<Interval>();
				list.add(this.intv);
			} else if (left == null) {
				list = new ArrayList<Interval>();
				list.add(this.intv);
				list.addAll(right.getIntervals());
			} else if (right == null) {
				list = left.getIntervals();
				list.add(this.intv);
			} else {
				list = left.getIntervals();
				list.add(this.intv);
				list.addAll(right.getIntervals());
			}
			return list;
		}

		/**
		 * Returns all intervals contained by this <code>IntervalTreeSet</code>
		 * which are also within <code>interval</code>. Intervals contained by
		 * this that partly overlaps <code>interval</code> are trimmed to be
		 * contained by <code>interval</code>.
		 * 
		 * @param interval The interval.
		 * @return All intervals contained by this <code>IntervalTreeSet</code>
		 *         which are also within <code>interval</code>.
		 */
		public ArrayList<Interval> getIntervals(Interval interval) {
			ArrayList<Interval> list = null;
			if (interval.end < this.intv.start) {
				list = (left == null ? new ArrayList<Interval>() : left
						.getIntervals(interval));
			} else if (interval.start > this.intv.end) {
				list = (right == null ? new ArrayList<Interval>() : right
						.getIntervals(interval));
			} else if (this.intv.contains(interval)) {
				list = new ArrayList<Interval>();
				list.add(interval);
			} else if (interval.contains(this.intv)) {
				list = (left == null ? new ArrayList<Interval>() : left
						.getIntervals(interval));
				list.add(this.intv);
				if (right != null)
					list.addAll(right.getIntervals(interval));
			} else if (this.intv.inInterval(interval.end)) {
				list = (left == null ? new ArrayList<Interval>() : left
						.getIntervals(interval));
				list.add(new Interval(this.intv.start, interval.end));
			} else if (this.intv.inInterval(interval.start)) {
				list = new ArrayList<Interval>();
				list.add(new Interval(interval.start, this.intv.end));
				if (right != null)
					list.addAll(right.getIntervals(interval));
			}
			return list;
		}

		/**
		 * Adds <code>interval</code> to this <code>IntervalTreeSet</code>.
		 * Returns <code>true</code> if this <code>IntervalTreeSet</code>
		 * changed as a result of the call.
		 * 
		 * @param interval The interval to add.
		 * @return <code>true</code> if this <code>IntervalTreeSet</code>
		 *         changed as a result of the call, otherwise <code>false</code>
		 *         .
		 */
		public boolean addInterval(Interval interval) {
			if (interval == null)
				throw new IllegalArgumentException(
						"Can't add an interval that's null.");

			System.out.println("IntervalTreeSet: Adding interval: "
					+ interval.toString());

			// If this node's interval and given interval are equal.
			if (this.intv.equals(interval))
				return false; // interval already existed

			// Treat if this node's interval contains given interval
			else if (this.intv.contains(interval))
				return false; // interval already contained

			// Treat non-overlapping intervals
			else if (interval.end + 1 < this.intv.start) {
				if (left == null) {
					left = new Node(interval);
					return true;
				} else {
					return left.addInterval(interval);
				}
			} else if (interval.start - 1 > this.intv.end) {
				if (right == null) {
					right = new Node(interval);
					return true;
				} else {
					return right.addInterval(interval);
				}
			}

			// Now we know that the intervals are overlapping somehow.
			// Let's see if the new start lies before the old start.
			if (interval.start < this.intv.start) {
				// Extend interval of this node to the left.
				this.intv = new Interval(interval.start, this.intv.end);

				if (left != null) {
					// Inform left child on change.
					left.superNodeExtendedToLeft(this);
				}
			}

			// Now let's see if the new end lies after the old end.
			if (interval.end > this.intv.end) {

				// Extend interval of this node to the right.
				this.intv = new Interval(this.intv.start, interval.end);

				if (right != null) {
					// Inform right child on change.
					right.superNodeExtendedToRight(this);
				}
			}

			// Changes definitely happened if we got here.
			return true;
		}

		/*
		 * This method is invoked when a super node to this node extended to the
		 * left and thus may overlap with this node or any of its child nodes.
		 */
		private void superNodeExtendedToLeft(Node superNode) {

			// If extended interval starts before this node's interval, remove
			// this node in favor of the left child. If left child not null,
			// pass on call to it.
			if (superNode.intv.start < this.intv.start) {
				superNode.left = this.left;
				if (left != null)
					left.superNodeExtendedToLeft(superNode);
			}

			// Else if extended interval starts within this node's interval, or
			// if it starts immediately after it, then extend the supernodes
			// interval and remove this node in favor of the left child.
			else if (this.intv.start <= superNode.intv.start
					&& superNode.intv.start <= this.intv.end + 1) {
				superNode.intv = new Interval(this.intv.start,
						superNode.intv.end);
				superNode.left = this.left;
			}

			// Else the extended interval lies after (and not immediately after)
			// this node's interval, pass on call to right child if it's not
			// null.
			else {
				if (right != null) {
					right.superNodeExtendedToLeft(superNode);
				}
			}
		}

		/*
		 * This method is invoked when a super node to this node extended to the
		 * right and thus may overlap with this node or any of its child nodes.
		 */
		private void superNodeExtendedToRight(Node superNode) {

			// If extended interval ends after this node's interval ends, remove
			// this node in favor of the right child. If right child not null,
			// pass on call to it.
			if (superNode.intv.end > this.intv.end) {
				superNode.right = this.right;
				if (right != null)
					right.superNodeExtendedToRight(superNode);
			}

			// Else if extended interval ends within this node's interval, or
			// if it ends immediately before it, then extend the supernodes
			// interval and remove this node in favor of the right child.
			else if (this.intv.start - 1 <= superNode.intv.end
					&& superNode.intv.end <= this.intv.end) {
				superNode.intv = new Interval(superNode.intv.start,
						this.intv.end);
				superNode.right = this.right;
			}

			// Else the extended interval lies before (and not immediately
			// before) this node's interval, pass on call to left child if it's
			// not null.
			else {
				if (left != null) {
					left.superNodeExtendedToLeft(superNode);
				}
			}
		}

		/**
		 * Remove the given interval from this <code>IntervalTreeSet</code>.
		 * Returns the new root.
		 */
		public Node remove(Interval interval) {

			// If the interval lies entirely before this node's
			// interval, pass on call to the left.
			if (interval.end < this.intv.start) {
				if (left != null) {
					left.remove(interval);
				}
				return this;
			}

			// Else if the interval lies entirely after this node's interval,
			// pass on call to right.
			else if (this.intv.end < interval.start) {
				if (right != null) {
					right.remove(interval);
				}
				return this;
			}

			// Else if the interval starts before this node's interval...
			else if (interval.start < this.intv.start) {

				// We already know that the interval doesn't end before this
				// node's interval starts. So inform left node on that if
				// doesn't have to worry about cutting to much on its right.
				if (left != null)
					left = left.removeRightOfStart(interval);

				// ... and if the interval ends before this node's interval
				// ends. Then just update this node's interval.
				if (interval.end < this.intv.end) {
					this.intv = new Interval(interval.end + 1, this.intv.end);
					return this;
				}

				// ... and if the interval's end equals this node's interval's
				// end. Then exchange this node for the leftmost of node's still
				// being right of this one. If no such node exists, we are done
				// and returns left node.
				else if (interval.end == this.intv.end) {
					if (right == null) {
						return left;
					} else if (right.left == null) {
						// Right is new root now.
						right.left = this.left;
						return right;
					} else {
						// Whatever is returned is new root.
						this.intv = right.removeLeftMost().intv;
						return this;
					}
				}

				// ... and if the interval's end is after this node's interval's
				// end. Then have right node remove everything left of
				// interval's endtime from the right child, then remove this
				// node.
				else { // interval.end > this.intv.end
					if (right != null)
						right = right.removeLeftOfEnd(interval);
					return this.removeNode();
				}
			}

			// Else if the interval's start equals this node's interval's start
			// ...
			else if (interval.start == this.intv.start) {

				// ... and the interval's end is before this node's interval's
				// end. Then adjust this node's interval.
				if (interval.end < this.intv.end) {
					this.intv = new Interval(interval.end + 1, this.intv.end);
					return this;
				}

				// ... and the interval's end equals this node's interval's end.
				// Then remove this node.
				else if (interval.end == this.intv.end) {
					return this.removeNode();
				}

				// ... and the interval's end lies after this node's interval's
				// end. Then first remove all that is left of the interval's end
				// from the right child and then remove this node.
				else { // (interval.end > this.intv.end)
					if (right != null)
						right = right.removeLeftOfEnd(interval);
					return this.removeNode();
				}
			}

			// Else the interval's start lies within this node's interval
			// (however not equal start times).
			else {
				// First, adjust this node's interval by removing the part
				// overlapping the interval.
				this.intv = new Interval(this.intv.start, interval.start - 1);

				// If the interval's end time is before this node's interval's
				// end. Then split this node's interval in two, making two
				// nodes, left node parent to right node.
				if (interval.end < this.intv.end) {
					Node newRightNode = new Node(new Interval(interval.end + 1,
							this.intv.end));
					newRightNode.right = this.right;
					this.right = newRightNode;
					return left; // Left node is now root.
				}

				// Else if the interval's end equals this node's interval's end.
				// Then what we've already done is enough and this node
				// continues to be the root.
				else if (interval.end == this.intv.end) {
					return this;
				}

				// Else the interval's end lies after this node's interval's
				// end. Then first remove all that is left of the interval's end
				// from the right child then return this node as it continues to
				// be the root.
				else {
					if (right != null)
						right = right.removeLeftOfEnd(interval);
					return this;
				}
			}
		}

		/*
		 * Remove this node by replacing it with either rightmost node in its
		 * left subtree or leftmost node in right subtree. Return the new root
		 * of the subtree.
		 */
		private Node removeNode() {
			if (left != null) { // Take root from left subtree
				if (left.right == null) {
					left.right = this.right;
					return left;
				} else {
					this.intv = left.removeRightMost().intv;
					return this;
				}
			} else if (right != null) { // Take root from right subtree
				if (right.left == null) {
					right.left = this.left;
					return right;
				} else {
					this.intv = right.removeLeftMost().intv;
					return this;
				}
			} else {
				return null; // Nothing's left after removal.
			}
		}

		/*
		 * This node's left child must not be null, cause a node can't remove
		 * itself, this must be taken care of outside this method.
		 */
		private Node removeLeftMost() {
			if (left == null) {
				return null;
			} else if (left.left == null) {
				Node ret = left;
				left = null;
				return ret;
			} else {
				return left.removeLeftMost();
			}
		}

		/*
		 * This node's right child must not be null, cause a node can't remove
		 * itself, this must be taken care of outside this method.
		 */
		private Node removeRightMost() {
			if (right == null) {
				return null;
			} else if (right.right == null) {
				Node ret = right;
				right = null;
				return ret;
			} else {
				return right.removeRightMost();
			}
		}

		/*
		 * Returns the most shallow node in the subtree having this node as root
		 * that can't be removed given the specified removal.
		 */
		private Node removeRightOfStart(Interval interval) {

			// If interval starts before this node's interval, return whatever
			// left node returns.
			if (interval.start < this.intv.start) {
				return (left == null ? null : left.removeRightOfStart(interval));
			}

			// Else if interval start is equal to this node's interval's start,
			// return left node.
			else if (interval.start == this.intv.start) {
				return left;
			}

			// Else if interval starts after this node's interval starts but
			// before or equal to immediately after this node's interval ends,
			// then adjust this node's interval and remove everything on the
			// right.
			else if (this.intv.start < interval.start
					&& interval.start <= this.intv.end + 1) {
				this.right = null;
				this.intv = new Interval(this.intv.start, interval.start - 1);
				return this;
			}

			// Else interval starts after (and not immediately after) this
			// node's interval ends, then return this node after setting right
			// child node to whatever right returns.
			else {
				if (right != null) {
					right = right.removeRightOfStart(interval);
				}
				return this;
			}
		}

		/*
		 * Returns the most shallow node in the subtree having this node as root
		 * that can't be removed given the specified removal.
		 */
		private Node removeLeftOfEnd(Interval interval) {

			// If interval ends after this node's interval, return whatever
			// right node returns.
			if (interval.end > this.intv.end) {
				return (right == null ? null : right.removeLeftOfEnd(interval));
			}

			// Else if interval end is equal to this node's interval's end,
			// return right node.
			else if (interval.end == this.intv.end) {
				return right;
			}

			// Else if interval ends before this node's interval ends but
			// after or equal to immediately before this node's interval starts,
			// then adjust this node's interval and remove everything on the
			// left.
			else if (this.intv.end > interval.end
					&& interval.end >= this.intv.start - 1) {
				this.left = null;
				this.intv = new Interval(interval.end + 1, this.intv.end);
				return this;
			}

			// Else interval ends before (and not immediately before) this
			// node's interval starts, then return this node after setting left
			// child node to whatever left returns.
			else {
				if (left != null) {
					left = left.removeRightOfStart(interval);
				}
				return this;
			}
		}

		/**
		 * Cut this <code>IntervalTreeSet</code> at both ends so only intervals
		 * that lie in the given interval are kept.
		 * 
		 * @return the new root of the tree.
		 */
		public Node cut(Interval interval) {
			if (interval == null)
				throw new IllegalArgumentException("Interval must not be null.");

			// If interval lies entirely before this node's interval, pass on
			// call to the left.
			if (interval.end < this.intv.start) {
				return (left == null ? null : left.cut(interval));
			}

			// If interval lies entirely after this node's interval, pass
			// on call to the right.
			if (this.intv.end < interval.start) {
				return (right == null ? null : right.cut(interval));
			}

			// If interval starts before this node's interval, call cutLeft() on
			// this node's left child for left sub-nodes to adjust. Else, adjust
			// start of this node's interval to the cut and set left child node
			// to null.
			if (interval.start < this.intv.start) {
				if (left != null) {
					this.left = left.cutLeft(interval);
				}
			} else {
				this.left = null;
				this.intv = new Interval(interval.start, this.intv.end);
			}

			// If interval ends after this node's interval, call cutRight() on
			// this node's right child for right sub-nodes to adjust. Else,
			// adjust end of this node's interval to the cut and set right child
			// node to null.
			if (interval.start < this.intv.start) {
				if (right != null) {
					this.right = right.cutRight(interval);
				}
			} else {
				this.right = null;
				this.intv = new Interval(this.intv.start, interval.end);
			}

			return this;
		}

		/*
		 * Returns the most shallow node in the subtree having this node as root
		 * that can't be removed given the specified cut.
		 */
		private Node cutLeft(Interval interval) {

			// If interval starts before this node's interval, return this node
			// and pass on call to left node to decide left node of this node.
			if (interval.start < this.intv.start) {
				if (left != null) {
					this.left = left.cutLeft(interval);
				}
				return this;
			}

			// Else if interval start lies within this node's interval, adjust
			// this node's interval, return this node and throw away left node.
			else if (this.intv.start <= interval.start
					&& interval.start <= this.intv.end) {
				this.left = null;
				this.intv = new Interval(interval.start, this.intv.end);
				return this;
			}

			// Else interval starts after this node's interval ends, then return
			// whatever right node returns. This will cause this node to be
			// thrown away.
			else {
				if (right != null) {
					return right.cutLeft(interval);
				} else {
					return null;
				}
			}
		}

		/*
		 * Returns the most shallow node in the subtree having this node as root
		 * that can't be removed given the specified cut.
		 */
		private Node cutRight(Interval interval) {

			// If interval ends after this node's interval, return this node
			// and pass on call to right node to decide right node of this node.
			if (interval.end > this.intv.end) {
				if (right != null) {
					this.right = right.cutRight(interval);
				}
				return this;
			}

			// Else if interval end lies within this node's interval, adjust
			// this node's interval, return this node and throw away right node.
			else if (this.intv.start <= interval.end
					&& interval.end <= this.intv.end) {
				this.right = null;
				this.intv = new Interval(this.intv.end, interval.end);
				return this;
			}

			// Else interval ends before this node's interval starts, then
			// return whatever left node returns. This will cause this node to
			// be thrown away.
			else {
				if (left != null) {
					return left.cutRight(interval);
				} else {
					return null; // (Base case in recursion.)
				}
			}
		}

		public int childrenAndSelfCount() {
			int leftChildren = (left == null ? 0 : left.childrenAndSelfCount());
			int rightChildren = (right == null ? 0 : right
					.childrenAndSelfCount());
			return 1 + leftChildren + rightChildren;
		}

		// /*
		// * Returns the depth of this node.
		// */
		// private int depth() {
		// int leftDepth = (left == null ? 0 : left.depth());
		// int rightDepth = (right == null ? 0 : right.depth());
		// return 1 + Math.max(leftDepth, rightDepth);
		// }
	}

	private Node root;

	/**
	 * Adds the specified interval to this <code>IntervalTreeSet</code>.
	 * 
	 * @param interval the interval to add
	 * @return true if this <code>IntervalTreeSet</code> changed as a result of
	 *         the call
	 */
	public boolean add(Interval interval) {
		if (root == null) {
			root = new Node(interval);
			return true;
		} else {
			return root.addInterval(interval);
		}

		// TODO balance
	}

	/**
	 * Adds all intervals in <code>intervals</code> to this
	 * <code>IntervalTreeSet</code>.
	 * 
	 * @param intervals the intervals to add
	 * @return true if this <code>IntervalTreeSet</code> changed as a result of
	 *         the call
	 */
	public boolean addAll(Collection<? extends Interval> intervals) {
		boolean ret = false;
		for (Interval interval : intervals) {
			ret = root.addInterval(interval);
		}
		return ret;

		// TODO balance
	}

	/**
	 * Removes the specified interval from this <code>IntervalTreeSet</code>.
	 * 
	 * @param interval the interval to be removed
	 */

	public void remove(Interval interval) {
		if (root != null) {
			root = root.remove(interval);
		}
	}

	/**
	 * Cuts this <code>IntervalTreeSet</code> at both ends so only intervals
	 * that lie in the given interval are kept. Intervals that are partly within
	 * the given interval will be cut.
	 * 
	 * @param interval the interval to cut after
	 */
	public void cut(Interval interval) {
		if (root != null) {
			root = root.cut(interval);
		}
	}

	/**
	 * Clears this <code>IntervalTreeSet</code>.
	 */
	public void clear() {
		root = null;
	}

	/**
	 * Returns true if this <code>IntervalTreeSet</code> is empty.
	 * 
	 * @return true if this <code>IntervalTreeSet</code> is empty
	 */
	public boolean isEmpty() {
		return root == null;
	}

	/**
	 * Returns number of intervals contained by this
	 * <code>IntervalTreeSet</code>. Note that size may change change in
	 * unexpected ways as intervals may be merged and separated by adds and
	 * removals.
	 * 
	 * @return number of intervals contained by this
	 *         <code>IntervalTreeSet</code>
	 */
	public int size() {
		return root == null ? 0 : root.childrenAndSelfCount();
	}

	/**
	 * Returns all intervals contained by this <code>IntervalTreeSet</code>.
	 * 
	 * @return all intervals contained by this <code>IntervalTreeSet</code>.
	 */
	public ArrayList<Interval> getIntervals() {
		return root == null ? new ArrayList<Interval>() : root.getIntervals();
	}

	/**
	 * Returns the intervals contained by this <code>IntervalTreeSet</code> and
	 * that lies, partly or fully, within the specified interval. If an interval
	 * is partly within by the given interval, the parts that are not within the
	 * specified interval will be cut off.
	 * 
	 * @return the intervals contained by this <code>IntervalTreeSet</code> and
	 *         that lies, partly or fully, within the specified interval.
	 */
	public ArrayList<Interval> getIntervals(Interval interval) {
		return root == null ? new ArrayList<Interval>() : root
				.getIntervals(interval);
	}
}
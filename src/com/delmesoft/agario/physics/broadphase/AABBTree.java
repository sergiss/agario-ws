package com.delmesoft.agario.physics.broadphase;

import java.util.List;


/*
	Copyright (c) 2009 Erin Catto http://www.box2d.org
	Copyright (c) 2016-2018 Lester Hedges <lester.hedges+aabbcc@gmail.com>
	This software is provided 'as-is', without any express or implied
	warranty. In no event will the authors be held liable for any damages
	arising from the use of this software.
	Permission is granted to anyone to use this software for any purpose,
	including commercial applications, and to alter it and redistribute it
	freely, subject to the following restrictions:
	1. The origin of this software must not be misrepresented; you must not
	   claim that you wrote the original software. If you use this software
	   in a product, an acknowledgment in the product documentation would be
	   appreciated but is not required.
	2. Altered source versions must be plainly marked as such, and must not be
	   misrepresented as being the original software.
	3. This notice may not be removed or altered from any source distribution.
	
	This code was adapted from parts of the Box2D Physics Engine,
	http://www.box2d.org
*/
public class AABBTree<T extends AABB> implements DynamicTree<T> {

	private static final float DEFAULT_EXTENSION = 0.1F;

	private final AABB combinedAABB;

	private Node[] nodeStack;
	private Node[] freeNodes;
	private int freeSize;

	private Node root;

	private float extension;

	public AABBTree() {
		this(DEFAULT_EXTENSION);
	}

	public AABBTree(float extension) {
		this.extension = extension;

		combinedAABB = new AABB();
		freeNodes = new Node[8];
		nodeStack = new Node[8];
	}

	@Override
	public void insert(T e) {
		Node node = obtain();
		// Fatten the aabb
		node.aabb.min.x = e.min.x - extension;
		node.aabb.min.y = e.min.y - extension;
		node.aabb.max.x = e.max.x + extension;
		node.aabb.max.y = e.max.y + extension;
		node.data = e;
		e.node = node;
		insertLeaf(node);
	}

	private void insertLeaf(Node leaf) {
		if (root == null) {
			root = leaf;
		} else {
			// find the best sibling
			AABB leafAABB = leaf.aabb;
			Node left, right, node = root;
			while (node.left != null) {

				left  = node.left;
				right = node.right;

				float area = node.aabb.getPerimeter();

				combinedAABB.combine(node.aabb, leafAABB);
				float combinedArea = combinedAABB.getPerimeter();

				// Cost of creating a new parent for this node and the new leaf
				float cost = 2.0f * combinedArea;

				// Minimum cost of pushing the leaf further down the tree
				float inheritanceCost = 2.0f * (combinedArea - area);

				// Cost of descending into left
				float cost1;
				if (left.left == null) {
					combinedAABB.combine(leafAABB, left.aabb);
					cost1 = combinedAABB.getPerimeter() + inheritanceCost;
				} else {
					combinedAABB.combine(leafAABB, left.aabb);
					float oldArea = left.aabb.getPerimeter();
					float newArea = combinedAABB.getPerimeter();
					cost1 = (newArea - oldArea) + inheritanceCost;
				}

				// Cost of descending into right
				float cost2;
				if (right.left == null) {
					combinedAABB.combine(leafAABB, right.aabb);
					cost2 = combinedAABB.getPerimeter() + inheritanceCost;
				} else {
					combinedAABB.combine(leafAABB, right.aabb);
					float oldArea = right.aabb.getPerimeter();
					float newArea = combinedAABB.getPerimeter();
					cost2 = newArea - oldArea + inheritanceCost;
				}

				// Descend according to the minimum cost.
				if (cost < cost1 && cost < cost2) {
					break;
				}

				// Descend
				if (cost1 < cost2) {
					node = left;
				} else {
					node = right;
				}
				
			}

			Node sibling = node;
			Node oldParent = sibling.parent;
			final Node newParent = obtain();
			newParent.parent = oldParent;
			newParent.data = null;
			newParent.aabb.combine(leafAABB, sibling.aabb);
			newParent.height = sibling.height + 1;

			if (oldParent != null) {
				// The sibling was not the root.
				if (oldParent.left == sibling) {
					oldParent.left = newParent;
				} else {
					oldParent.right = newParent;
				}

				newParent.left = sibling;
				newParent.right = leaf;
				sibling.parent = newParent;
				leaf.parent = newParent;
			} else {
				// The sibling was the root.
				newParent.left = sibling;
				newParent.right = leaf;
				sibling.parent = newParent;
				leaf.parent = newParent;
				root = newParent;
			}

			// Walk back up the tree fixing heights and AABBs
			node = leaf.parent;
			while (node != null) {
				node = balance(node);

				left  = node.left;
				right = node.right;

				node.height = max(left.height, right.height) + 1;
				node.aabb.combine(left.aabb, right.aabb);

				node = node.parent;
			}

		}
		
	}
	
	@Override
	public void remove(T e) {

		Node leaf = e.node;
		e.node = null;

		if (leaf == root) {
			root = null;
			free(leaf);
		} else {

			Node parent = leaf.parent;
			Node grandParent = parent.parent;
			Node sibling;
			if (parent.left == leaf) {
				sibling = parent.right;
			} else {
				sibling = parent.left;
			}

			if (grandParent != null) {
				// Destroy parent and connect sibling to grandParent.
				if (grandParent.left == parent) {
					grandParent.left = sibling;
				} else {
					grandParent.right = sibling;
				}
				sibling.parent = grandParent;

				free(parent);

				// Adjust ancestor bounds.
				Node left, right, node = grandParent;
				while (node != null) {
					node = balance(node);

					left  = node.left;
					right = node.right;

					node.height = max(left.height, right.height) + 1;
					node.aabb.combine(left.aabb, right.aabb);

					node = node.parent;
				}
			} else {
				root = sibling;
				sibling.parent = null;
				free(parent);
			}

		}

	}
	
	private Node obtain() {
		Node node;
		if(freeSize == 0) {
			node = new Node();
		} else {
			node = freeNodes[--freeSize];
		}			
		return node;
	}
	
	private void free(Node node) {
		node.data = null;
		node.parent = node.left = node.right = null;
		node.height = 0;		
		if(freeSize == freeNodes.length) {
			Node[] tmp = new Node[(int) (freeSize * 1.75)];
			System.arraycopy(freeNodes, 0, tmp, 0, freeSize);
			freeNodes = tmp;
		}
		freeNodes[freeSize++] = node;
	}

	private Node balance(final Node a) {

		if (a.left == null || a.height < 2) {
			return a;
		}
		
		final Node b = a.left;
		final Node c = a.right;

		int balance = c.height - b.height;

		// Rotate C up
		if (balance > 1) {
			final Node f = c.left;
			final Node g = c.right;
			// Swap A and C
			c.left = a;
			c.parent = a.parent;
			a.parent = c;

			// A's old parent should point to C
			if (c.parent != null) {
				if (c.parent.left == a) {
					c.parent.left = c;
				} else {
					c.parent.right = c;
				}
			} else {
				root = c;
			}

			// Rotate
			if (f.height > g.height) {
				c.right = f;
				a.right = g;
				g.parent = a;
				a.aabb.combine(b.aabb, g.aabb);
				c.aabb.combine(a.aabb, f.aabb);

				a.height = 1 + max(b.height, g.height);
				c.height = 1 + max(a.height, f.height);
			} else {
				c.right = g;
				a.right = f;
				f.parent = a;
				a.aabb.combine(b.aabb, f.aabb);
				c.aabb.combine(a.aabb, g.aabb);

				a.height = max(b.height, f.height) + 1;
				c.height = max(a.height, g.height) + 1;
			}

			return c;
		}

		// Rotate B up
		if (balance < -1) {
			
			final Node d = b.left;
			final Node e = b.right;

			// Swap A and B
			b.left = a;
			b.parent = a.parent;
			a.parent = b;

			// A's old parent should point to B
			if (b.parent != null) {
				if (b.parent.left == a) {
					b.parent.left = b;
				} else {
					b.parent.right = b;
				}
			} else {
				root = b;
			}

			// Rotate
			if (d.height > e.height) {
				b.right = d;
				a.left  = e;
				e.parent = a;
				a.aabb.combine(c.aabb, e.aabb);
				b.aabb.combine(a.aabb, d.aabb);

				a.height = max(c.height, e.height) + 1;
				b.height = max(a.height, d.height) + 1;
			} else {
				b.right = e;
				a.left  = d;
				d.parent = a;
				a.aabb.combine(c.aabb, d.aabb);
				b.aabb.combine(a.aabb, e.aabb);

				a.height = max(c.height, d.height) + 1;
				b.height = max(a.height, e.height) + 1;
			}

			return b;
		}

		return a;
	}

	@Override
	public void update(T e) {
		if(!e.node.aabb.contains(e)) {
			remove(e);
			insert(e);
		}
	}

	@Override
	public void clear() {
		free(root);
		root = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void iterate(AABB aabb, Iterator<T> it) {
		int nodeStackIndex = 0;
		nodeStack[nodeStackIndex++] = root;
		Node node;
		while (nodeStackIndex > 0) {
			node = nodeStack[--nodeStackIndex];
			nodeStack[nodeStackIndex] = null; // GC :)
			if (node != null && node.aabb.overlap(aabb)) {
				if (node.left == null) {
					if (!it.next((T) node.data)) { // Iterate
						return; // stop by return user value
					}
				} else {
					if (nodeStackIndex + 2 >= nodeStack.length) {
						Node[] tmp = new Node[nodeStackIndex << 1];
						System.arraycopy(nodeStack, 0, tmp, 0, nodeStack.length);
						nodeStack = tmp;
					}
					nodeStack[nodeStackIndex++] = node.left;
					nodeStack[nodeStackIndex++] = node.right;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void query(AABB aabb, List<T> store) {
		int nodeStackIndex = 0;
		nodeStack[nodeStackIndex++] = root;

		Node node;
		while (nodeStackIndex > 0) {
			node = nodeStack[--nodeStackIndex];
			nodeStack[nodeStackIndex] = null; // GC :)
			if (node != null && node.aabb.overlap(aabb)) {
				if (node.left == null) {
					store.add((T) node.data);
				} else {
					if (nodeStackIndex + 2 >= nodeStack.length) {
						Node[] tmp = new Node[nodeStackIndex << 1];
						System.arraycopy(nodeStack, 0, tmp, 0, nodeStack.length);
						nodeStack = tmp;
					}
					nodeStack[nodeStackIndex++] = node.left;
					nodeStack[nodeStackIndex++] = node.right;
				}
			}
		}
	}

	private static int max(int a, int b) {
		return a > b ? a : b;
	}

}

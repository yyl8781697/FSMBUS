package ucas.iie.graph.action;

import java.util.ArrayList;

/**
 * Ullmann for http://blog.csdn.net/sh_c1991/article/details/42746429
 * @author Administrator
 *
 */
public class IsomorphismImpl {
	public static boolean isIsomorphism(GraphBean subgraph, GraphBean graphdb) {
		//fast check
		if(subgraph.getvList().size()!=graphdb.getvList().size()
				|| subgraph.geteList().size()!=graphdb.geteList().size())
		{
			return false;
		}
			
		
		
		int M0[][] = getMatrixM(subgraph, graphdb);
		int MA[][] = subgraph.getMatrix();
		int MB[][] = graphdb.getMatrix();
		ArrayList<EdgeBean> ebq = subgraph.geteList();
		ArrayList<EdgeBean> ebdb = graphdb.geteList();
		for (int i = 0; i < subgraph.getvList().size(); i++) {
			for (int j = 0; j < graphdb.getvList().size(); j++) {
				if (M0[i][j] == 1) {
					String ilabel = subgraph.getvList().get(i).getLabel();
					String jlabel = graphdb.getvList().get(j).getLabel();
					if (ilabel.equals(jlabel)) {
						for (int x = 0; x < subgraph.getvList().size(); x++) {
							boolean tag = false;
							if (MA[i][x] == 1) {
								String label_ix = getLabel(String.valueOf(i),
										String.valueOf(x), ebq);
								for (int y = 0; y < graphdb.getvList().size(); y++) {
									if (M0[x][y] * MB[y][j] == 1) {
										String label_yj = getLabel(
												String.valueOf(y),
												String.valueOf(j), ebdb);
										if (label_ix.equals(label_yj)) {
											tag = true;
											break;
										}
									}
								}// break
								if (!tag) {
									M0[i][j] = 0;
									break;
								}
							}
						}// break
					} else {// if(ilabel.equals(jlabel))
						M0[i][j] = 0;
					}
				}// if (M0[i][j] == 1)
			}
		}
		// System.out.println("M':");
		for (int i = 0; i < subgraph.getvList().size(); i++) {
			int sumi = 0;
			for (int j = 0; j < graphdb.getvList().size(); j++) {
				// System.out.print(M0[i][j] + " ");
				sumi += M0[i][j];
			}
			if (sumi == 0) {
				return false;
			}
		}
		int raw = subgraph.getvList().size();
		int col = graphdb.getvList().size();
		int F[] = new int[col];// F[i] = 1 
		int H[] = new int[raw];// H[d] = k 
		int d = 0;// 
		int k = 0;// 
		int[][][] matrixList = new int[raw][][];// 
		for (int i = 0; i < F.length; i++) {
			F[i] = -1;
		}
		for (int i = 0; i < H.length; i++) {
			H[i] = -1;
		}

		// //////////////////////////////
		while (true) {

			if (H[d] == -1) {
				k = 0;
				matrixList[d] = M0;
			} else {// 
				k = H[d] + 1;
				F[H[d]] = -1;
				M0 = matrixList[d];

			}

			while (k < col) {// 
				if (M0[d][k] == 1 && F[k] == -1) {// 
					break;
				}
				k++;
			}

			if (k == col) {// 
				H[d] = -1;
				d--;
			} else {// M0[d][k]=1,
				for (int j = 0; j < col; j++) {
					M0[d][j] = 0;
				}
				M0[d][k] = 1;
				H[d] = k;
				F[k] = 1;
				d++;
			}

			if (d == -1) {
				return false;
			}

			if (d == raw) {// 
				if (isTrueFor(MA, MB, M0)) {// 
					return true;
				} else {//
					d = raw - 1;
				}

			}// if
		}// while
	}

	/**
	 * 
	 * @param query
	 * @param db
	 */
	private static int[][] getMatrixM(GraphBean query, GraphBean db) {
		int row = query.getvList().size();
		int column = db.getvList().size();
		int[][] M0 = new int[row][column];
		// System.out.println("M0:");
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < column; j++) {
				String vi = query.getvList().get(i).getVertex();
				String vj = db.getvList().get(j).getVertex();
				if (db.getVDegree(vj) >= query.getVDegree(vi))
					M0[i][j] = 1;
				else
					M0[i][j] = 0;
				// System.out.print(M0[i][j] + " ");
			}
			// System.out.println("");
		}
		return M0;
	}

	

	/**
	 * @param i
	 * @param j
	 * @param ebList
	 * @return
	 */
	private static String getLabel(String i, String j, ArrayList<EdgeBean> ebList) {
		for (int k = 0; k < ebList.size(); k++) {
			EdgeBean eb = ebList.get(k);
			String vi = eb.getVertex_i();
			String vj = eb.getVertex_j();
			if (i.equals(vi) && j.equals(vj))
				return eb.getLabel_e();
			else if (j.equals(vi) && i.equals(vj))
				return eb.getLabel_e();
		}
		return null;
	}

	

	/**
	 * for all element int MA[i][j]=1 ==> MC=M*{(M*MB)^T},MC[i][j]=1
	 * 
	 * @param MA
	 * @param MB
	 * @param M
	 * @return
	 */
	private static boolean isTrueFor(int[][] MA, int[][] MB, int M[][]) {
		boolean flag = true;
		int raw = M.length;
		int column = MB[0].length;
		int tmp[][] = new int[raw][column];// tmp[][]=M*MB
		for (int i = 0; i < raw; i++) {// raws
			for (int j = 0; j < column; j++) {// columns
				for (int k = 0; k < M[0].length; k++) {
					tmp[i][j] += M[i][k] * MB[k][j];
				}
			}
		}
		int tmp_t[][] = new int[column][raw];
		for (int i = 0; i < raw; i++) {// raws
			for (int j = 0; j < column; j++) {// columns
				tmp_t[j][i] = tmp[i][j];
			}
		}
		int MC[][] = new int[MA.length][MA[0].length];
		// System.out.println("MC:");
		for (int i = 0; i < MA.length; i++) {// raws
			for (int j = 0; j < MA[0].length; j++) {// columns
				for (int k = 0; k < M[0].length; k++) {
					MC[i][j] += M[i][k] * tmp_t[k][j];
				}
				// System.out.print(MC[i][j] + " ");
			}
			// System.out.println();
		}
		for (int i = 0; i < MA.length; i++) {// raws
			for (int j = 0; j < MA[0].length; j++) {// columns
				if (MA[i][j] == 1) {
					if (MC[i][j] == 1)
						continue;
					else
						return false;
				}
			}

		}
		return flag;
	}

	
}

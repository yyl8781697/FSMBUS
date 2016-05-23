package ucas.iie.graph.action;

import java.util.ArrayList;

public class GraphBean {
	/** 
	 *  
		v 0 2
		v 1 2
		v 2 2
		v 3 2
		v 4 2
		e 0 1 2
		e 1 2 2
		e 2 3 2
		e 3 4 2
	 */
	private ArrayList<VertexBean> vList;
	private ArrayList<EdgeBean> eList;
	public GraphBean(){
		vList = new ArrayList<VertexBean>();
		eList = new ArrayList<EdgeBean>();
	}
	public ArrayList<VertexBean> getvList() {
		return vList;
	}
	public void setvList(ArrayList<VertexBean> vList) {
		this.vList = vList;
	}
	public ArrayList<EdgeBean> geteList() {
		return eList;
	}
	public void seteList(ArrayList<EdgeBean> eList) {
		this.eList = eList;
	}	
	public int[][] getMatrix(){
		int[][] M = new int[vList.size()][vList.size()];
		for (int index = 0; index < eList.size(); index++) {
			EdgeBean eb = eList.get(index);
			int i = Integer.parseInt(eb.getVertex_i());
			int j = Integer.parseInt(eb.getVertex_j());
			M[i][j] = 1;
			M[j][i] = 1;
		}
//		System.out.println("MA/MB:");
//		for (int i = 0; i < vList.size(); i++) {
//			for (int j = 0; j < vList.size(); j++) {
//				System.out.print(M[i][j] + " ");
//			}
//			System.out.println();
//		}
		return M;
	}
	
	public int getVDegree(String v){
		int sumDeg = 0;
		for (int i = 0; i < eList.size(); i++) {
			if(eList.get(i).getVertex_i().equals(v)||
					eList.get(i).getVertex_j().equals(v)){
				sumDeg ++;
			}
		}
		return sumDeg;
	}
	public String toString(){
		String res = "";
		for (int i = 0; i < this.vList.size(); i++) {
			res +="v " + vList.get(i).getVertex()  + " " + vList.get(i).getLabel() + "\r\n";
		}
		for (int j = 0; j < this.eList.size(); j++) {
			res +="e " + eList.get(j).getVertex_i()  + " " + eList.get(j).getVertex_j() + " "+eList.get(j).getLabel_e() + "\r\n";
		}
		return res;
	}
	public static void main(String[] args) {
		new GraphBean().getMatrix();
	}
}

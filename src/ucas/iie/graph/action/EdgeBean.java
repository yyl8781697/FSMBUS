package ucas.iie.graph.action;

public class EdgeBean {//“e i j k”表示图的边<i,j>的label是k//“e i j k”表示图的边<i,j>的label是k
	/** 图的数据格式
	 *  t # 0 表示第0个图
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
	private String vertex_i;
	private String vertex_j;
	private String label_e;
	public String getVertex_i() {
		return vertex_i;
	}
	public void setVertex_i(String vertex_i) {
		this.vertex_i = vertex_i;
	}
	public String getVertex_j() {
		return vertex_j;
	}
	public void setVertex_j(String vertex_j) {
		this.vertex_j = vertex_j;
	}
	public String getLabel_e() {
		return label_e;
	}
	public void setLabel_e(String label_e) {
		this.label_e = label_e;
	}
}

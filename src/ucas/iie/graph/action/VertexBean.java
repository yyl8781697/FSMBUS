package ucas.iie.graph.action;


public class VertexBean { //“e i j k”表示图的边<i,j>的label是k
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
	private String vertex;
	private String label;
	public String getVertex() {
		return vertex;
	}
	public void setVertex(String vertex) {
		this.vertex = vertex;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
}

package ucas.iie.graph.action;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class IsomorphismTest {
	private ArrayList<GraphBean> query_g;// 
	private ArrayList<GraphBean> mydb_g;// 

	public IsomorphismTest() {
		query_g = new ArrayList<GraphBean>();
		mydb_g = new ArrayList<GraphBean>();
	}
	
	public ArrayList<GraphBean> getQuery_g() {
		return query_g;
	}

	public void setQuery_g(ArrayList<GraphBean> query_g) {
		this.query_g = query_g;
	}

	public ArrayList<GraphBean> getMydb_g() {
		return mydb_g;
	}

	public void setMydb_g(ArrayList<GraphBean> mydb_g) {
		this.mydb_g = mydb_g;
	}

	/**
	 * 
	 * @param queryFile
	 *           
	 * @param dbFile
	 *           
	 * @throws IOException
	 */
	public void initGraphDB(String queryFile, String dbFile) throws IOException {
		BufferedReader q_br = new BufferedReader(new InputStreamReader(
				new FileInputStream(queryFile)));
		String lineData = q_br.readLine();
		GraphBean qgb;
		if (lineData.startsWith("t #")) {// 
			qgb = new GraphBean();
			this.query_g.add(qgb);
			while ((lineData = q_br.readLine()) != null) {
				if (lineData.startsWith("t #")) {
					qgb = new GraphBean();
					this.query_g.add(qgb);
					continue;
				} else if (lineData.startsWith("v")) { // 
					String vs[] = lineData.split(" ");
					VertexBean vb = new VertexBean();
					vb.setVertex(vs[1]);
					vb.setLabel(vs[2]);
					qgb.getvList().add(vb);
				} else { // 
					String es[] = lineData.split(" ");
					EdgeBean eb = new EdgeBean();
					eb.setVertex_i(es[1]);
					eb.setVertex_j(es[2]);
					eb.setLabel_e(es[3]);
					qgb.geteList().add(eb);
				}
			}
		}

		BufferedReader db_br = new BufferedReader(new InputStreamReader(
				new FileInputStream(dbFile)));
		lineData = db_br.readLine();
		GraphBean dbgb;
		if (lineData.startsWith("t #")) {//
			dbgb = new GraphBean();
			this.mydb_g.add(dbgb);
			while ((lineData = db_br.readLine()) != null) {
				if (lineData.startsWith("t #")) {
					dbgb = new GraphBean();
					this.mydb_g.add(dbgb);
					continue;
				} else if (lineData.startsWith("v")) { //
					String vs[] = lineData.split(" ");
					VertexBean vb = new VertexBean();
					vb.setVertex(vs[1]);
					vb.setLabel(vs[2]);
					dbgb.getvList().add(vb);
				} else if (lineData.startsWith("e")) { // 
					String es[] = lineData.split(" ");
					EdgeBean eb = new EdgeBean();
					eb.setVertex_i(es[1]);
					eb.setVertex_j(es[2]);
					eb.setLabel_e(es[3]);
					dbgb.geteList().add(eb);
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		IsomorphismTest it = new IsomorphismTest();
		String queryFile = "C:\\querylist.txt";
		String dbFile = "C:\\graphlist.txt";
		try {
			it.initGraphDB(queryFile, dbFile);
			ArrayList<GraphBean> query_g = it.getQuery_g();
			System.out.println("子图(size)" + query_g.size());
			ArrayList<GraphBean> db_g = it.getMydb_g();
			System.out.println("单图(size)" + db_g.size());
			for (int i = 0; i < query_g.size(); i++) {
				for (int j = 0; j < db_g.size(); j++) {
					GraphBean tq = query_g.get(i);
					GraphBean tdb = db_g.get(j);
					if (IsomorphismImpl.isIsomorphism(tq, tdb)) {
						System.err.println("t # " + i + " 与  T # " + j + "同构ͬ");
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

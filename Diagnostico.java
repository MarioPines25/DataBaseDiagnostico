package DataBase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.*;


public class Diagnostico {

	private final String DATAFILE = "data/disease_data.data";
	private Connection conn;
	private Statement st;
	private void showMenu() {

		int option = -1;
		do {
			System.out.println("Bienvenido a sistema de diagn�stico\n");
			System.out.println("Selecciona una opci�n:\n");
			System.out.println("\t1. Creaci�n de base de datos y carga de datos.");
			System.out.println("\t2. Realizar diagn�stico.");
			System.out.println("\t3. Listar s�ntomas de una enfermedad.");
			System.out.println("\t4. Listar enfermedades y sus c�digos asociados.");
			System.out.println("\t5. Listar s�ntomas existentes en la BD y su tipo sem�ntico.");
			System.out.println("\t6. Mostrar estad�sticas de la base de datos.");
			System.out.println("\t7. Salir.");
			try {
				option = readInt();
				switch (option) {
				case 1:
					crearBD();
					break;
				case 2:
					realizarDiagnostico();
					break;
				case 3:
					listarSintomasEnfermedad();
					break;
				case 4:
					listarEnfermedadesYCodigosAsociados();
					break;
				case 5:
					listarSintomasYTiposSemanticos();
					break;
				case 6:
					mostrarEstadisticasBD();
					break;
				case 7:
					exit();
					break;
				}
			} catch (Exception e) {
				System.err.println("Opci�n introducida no v�lida!");
			}
		} while (option != 7);
		exit();
	}

	private void exit() {
		System.out.println("Saliendo.. �hasta otra!");
		System.exit(0);
	}

	private void conectar() throws Exception {
		// implementar
		String drv="com.mysql.jdbc.Driver";
		Class.forName(drv);
		/* conexion a la BD */
		String serverAddress="localhost:3306";
		String db= "diagnostico";
		String user = "bddx";
		String pass = "bddx_pwd";
		String url= "jdbc:mysql://"+ serverAddress+"/";
		conn = DriverManager.getConnection(url, user, pass);
		System.out.println("Conectado a la base de datos!");

	}

	private void crearBD() throws Exception{
		String s;
		PreparedStatement p = null;
		try {
			if(connection==null) {
				conectar();
			}
			
			PreparedStatement pst = connection.prepareStatement("CREATE SCHEMA IF NOT EXISTS `diagnostico`  DEFAULT CHARACTER SET utf8;");
			pst.executeUpdate();

			//CREACION DE TABLAS

			// Tabla disease:
			String disease = "CREATE TABLE IF NOT EXISTS `diagnostico`.`disease`("
					+ "`disease_id` INT NOT NULL," 
					+ "`name` VARCHAR(255) NOT NULL," 
					+ "PRIMARY KEY (`disease_id`));";
			p = connection.prepareStatement(disease);
			p.executeUpdate();
			p.close();	

			// Tabla symptom:
			String symptom ="CREATE TABLE IF NOT EXISTS `diagnostico`.`symptom` ("+
					"`cui` VARCHAR(25) NOT NULL," +
					"`name` VARCHAR(255) NOT NULL,"+
					"PRIMARY KEY (`cui`));";

			p = connection.prepareStatement(symptom);
			p.executeUpdate();
			p.close();	

			// Tabla source
			String source = "CREATE TABLE IF NOT EXISTS `diagnostico`.`source` ( "+
					"`source_id` INT NOT NULL," +
					"`name` VARCHAR(255) NOT NULL," + 
					"PRIMARY KEY (`source_id`));";
			p = connection.prepareStatement(source);
			p.executeUpdate();
			p.close();	

			// Tabla code
			String code="CREATE TABLE IF NOT EXISTS `diagnostico`.`code` ("+
					"`code` VARCHAR(255) NOT NULL,"+
					"`source_id_c` INT NOT NULL," +
					"PRIMARY KEY (`code`),"+
					"INDEX `source_id_c_idx` (`source_id_c` ASC)," +
					"CONSTRAINT `source_id_c`" +
					" FOREIGN KEY (`source_id_c`)" +
					" REFERENCES `diagnostico`.`source` (`source_id`)" +
					" ON DELETE NO ACTION"+
					" ON UPDATE NO ACTION);";
			p = connection.prepareStatement(code);
			p.executeUpdate();
			p.close();	

			// Tabla semantic_type
			String semantic_type = "CREATE TABLE IF NOT EXISTS `diagnostico`.`semantic_type` (" +
					"`semantic_type_id` INT NOT NULL," +
					"`cui` VARCHAR(45) NOT NULL," +
					"PRIMARY KEY (`semantic_type_id`));";
			p = connection.prepareStatement(semantic_type);
			p.executeUpdate();
			p.close();	

			// Tabla symptom_semantic_type
			String symptom_semantic_type = "CREATE TABLE IF NOT EXISTS `diagnostico`.`symptom_semantic_type` (" +
					"`cui_sst` VARCHAR(25) NOT NULL," +
					"`semantic_type_id_sst` INT NOT NULL," +
					"INDEX (`cui_sst` ASC)," +
					"INDEX `semantic_type_id_sst_idx` (`semantic_type_id_sst` ASC)," +
					" CONSTRAINT `cui_sst`"+
					" FOREIGN KEY (`cui_sst`)"+
					" REFERENCES `diagnostico`.`symptom` (`cui`)"+
					" ON DELETE NO ACTION"+
					" ON UPDATE NO ACTION,"+
					" CONSTRAINT `semantic_type_id_sst`"+
					" FOREIGN KEY (`semantic_type_id_sst`)"+
					" REFERENCES `diagnostico`.`semantic_type` (`semantic_type_id`)"+
					" ON DELETE NO ACTION"+
					" ON UPDATE NO ACTION);";
			p = connection.prepareStatement(symptom_semantic_type);
			p.executeUpdate();
			p.close();	

			// Tabla disease_symptom
			String disease_symptom = "CREATE TABLE IF NOT EXISTS `diagnostico`.`disease_symptom` (" +
					"`disease_id_ds` INT NOT NULL," +
					"`cui_ds` VARCHAR(45) NOT NULL," +
					"INDEX `disease_id_ds_idx` (`disease_id_ds` ASC)," +
					"INDEX `cui_ds_idx` (`cui_ds` ASC)," +
					" CONSTRAINT `disease_id_ds`" +
					" FOREIGN KEY (`disease_id_ds`)" +
					" REFERENCES `diagnostico`.`disease` (`disease_id`)"+
					" ON DELETE NO ACTION"+
					" ON UPDATE NO ACTION,"+
					" CONSTRAINT `cui_ds`" +
					" FOREIGN KEY (`cui_ds`)" +
					" REFERENCES `diagnostico`.`symptom` (`cui`)" +
					" ON DELETE NO ACTION" +
					" ON UPDATE NO ACTION);";
			p = connection.prepareStatement(disease_symptom);
			p.executeUpdate();
			p.close();	



			//Tabla disease_has_code
			String disease_has_code = "CREATE TABLE IF NOT EXISTS `diagnostico`.`disease_has_code` (" +
					"`disease_id_dhc` INT NULL," +
					"`code_dhc` VARCHAR(255) NULL," +
					"`source_id_dhc` INT NULL," +
					"INDEX `disease_id_dhc_idx` (`disease_id_dhc` ASC),"+
					"INDEX `code_dhc_idx` (`code_dhc` ASC)," +
					"INDEX `source_id_dhc_idx` (`source_id_dhc` ASC),"+
					" CONSTRAINT `disease_id_dhc`" +
					" FOREIGN KEY (`disease_id_dhc`)"+
					" REFERENCES `diagnostico`.`disease` (`disease_id`)" +
					" ON DELETE NO ACTION"+
					" ON UPDATE NO ACTION," +
					" CONSTRAINT `code_dhc`" +
					" FOREIGN KEY (`code_dhc`)"+
					" REFERENCES `diagnostico`.`code` (`code`)"+
					" ON DELETE NO ACTION" +
					" ON UPDATE NO ACTION,"+
					" CONSTRAINT `source_id_dhc`"+
					" FOREIGN KEY (`source_id_dhc`)" +
					" REFERENCES `diagnostico`.`source` (`source_id`)"+
					" ON DELETE NO ACTION"+
					" ON UPDATE NO ACTION);";
			p = connection.prepareStatement(disease_has_code);
			p.executeUpdate();
			p.close();	



			//Obtencion de los datos a traves del archivo DATA

						
						LinkedList<String> list = readData();
						String []enfermedades;
						String []codVoc;
						String []codigo;
						String[]enfSint;//array de enfermedades y sintomas
			
			
						for (int i = 0; i < list.size(); i++) {
							enfSint = list.get(i).split("=",2);
							//COMIENZO PARTE IZQUIERDA ARBOL
							enfermedades=enfSint[0].split(":");
							codVoc=enfermedades[1].split(";");
			
							for(int j=0;j<codVoc.length;j++){
								//conseguimos codigos y vocabularios
								codigo=codVoc[j].split("@");
							}
							//FIN PARTE IZQUIERDA ARBOL
			
							//COMIENZO PARTE DERECHA ARBOL
							String [] sintomas;
							String [] elementos;
							sintomas = enfSint[1].split(";");
							for (int j=0; j<sintomas.length;j++){
								System.out.println(sintomas[j]);
								elementos= sintomas[j].split(":");
								
						}
							
							
			/*
			 * A la salida de los bucles, los datos se distribuyen:
			 * 		enfermedades = 	contiene el nombre de todas las enfermedades (0-10 --> 11 enfermedades)
			 * 		codVoc = 		tiene el codigo y el vocabulario de cada enfermedad.
			 * 		codigo = 		posiciones pares: codigo
			 * 				 		posiciones impares: vocabulario
			 * 		elementos =		(diferencia entre elementos = 3)
			 * 						contiene el sintoma, su codigo y tipo semantico. 
			 * 
			 * Cada iteración ira haciendo esta separaciones por lo que debemos introducir en cada una
			 * de las tablas los datos necesarios de cada array.
			 */

						}
			
			
						
		}catch(SQLException ex) {
			System.err.println(ex.getMessage());
		}

	}


	private void realizarDiagnostico() throws Exception{
		int n = 0;
		//listarSintomasCui();
		ArrayList<String> sintomas = new ArrayList<>();
		System.out.println("Ingrese cod_sintoma: ");
		

		for(int i = 0; i < 6; i++) {
			String entrada = readString();
			sintomas.add(entrada);
			System.out.print("Ingresar otro sintoma?[s/n]");
			entrada = readString();

			if(!entrada.equals("n") && !entrada.equals("s")) {
				System.out.println("Error, introduce de nuevo el sintoma");
				boolean correcto = false;
				while (!correcto){
					System.out.print("[s/n]");
					entrada = readString();
					if(!entrada.equals("n") && !entrada.equals("s"))
						correcto = false;
					else
						correcto = true;
				}

				i--;

			}
			if(entrada.equals("n")) {
				n++;
				break;
			}
			else {
				n++;
			}	

		}
		System.out.println("Gracias, aquí tiene su diagnóstico");
		String list = "";

		if(n == 1){
			list += "symptom.cui = "+ sintomas.get(0);
			System.out.println(list);
		}
		if (n == 2){
			list += "symptom.cui = " + sintomas.get(0) + " AND ";
			list += "symptom.cui = " + sintomas.get(1);
			System.out.println(list);

		}
		if (n>2) {
			for (int i = 0; i < n-1; i++ ) {
				list += "symptom.cui = " + sintomas.get(i) + " AND ";
			}
			list += "symptom.cui = " + sintomas.get(n-1);
			System.out.println(list);
		}

		String sintoma = "SELECT symptom.nombre"
				+ "FROM Symptom"
				+ "WHERE symptom.cui = " + list + ";";

		//		Statement st =  conn.createStatement();
		//		ResultSet rs = st.executeQuery(sintoma);
		//		 while(rs.next()){
		//			 System.out.println(rs.getObject(2));
		//		 }

	}

	private void listarSintomasCui() { //metodo auxiliar para poder listar los sintomas y sus codigos (uso en realizarDiagnostico())
		try {
			String sintomas = "SELECT (Symptom.name) "
							+ "FROM Symptom;";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sintomas);
			
			String codSintomas = "SELECT (Symptom.cui) "
					+ "FROM Symptom;";
			Statement st1 = conn.createStatement();
			ResultSet rs1 = st1.executeQuery(codSintomas);
			
			while(rs.next() && rs1.next()) {
				System.out.println("Sintoma: " + rs.getObject(1) + " , Codigo: " + rs1.getObject(1));	

		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void listarSintomasEnfermedad() throws Exception {
		try {
			conectar();

			String nombre = "SELECT (disease.name) "
					+ "FROM Disease;";
			Statement st1 =  conn.createStatement();
			ResultSet rs1 = st1.executeQuery(nombre);

			String id = "SELECT (disease.disease_id) "
					+ "FROM Disease;";
			Statement st2 = conn.createStatement();
			ResultSet rs2 = st2.executeQuery(id);

			while(rs1.next() && rs2.next()) {
				System.out.println(rs1.getObject(1) + "/" + rs2.getObject(1));
			}

			System.out.println("Ingrese el ID de la enfermedad");
			int entrada = readInt();

			String cui = "SELECT cui " + "FROM disease_symptom "
					+ "WHERE disease_id= " + entrada + ";";
			PreparedStatement st3 = conn.prepareStatement(cui);
			ResultSet rs3 = st3.executeQuery(cui);

			while(rs3.next()) {
				String comp = rs3.getString(1);
				String cuicomp = "SELECT name " + "FROM Symptom " 
						+ "WHERE cui= " + comp + ";";
				Statement st4 = conn.createStatement();
				ResultSet rs4 = st4.executeQuery(cuicomp);
				if(rs4.next()) {
					System.out.println(rs4.getObject(1));
				}	
			}
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}


	private void listarEnfermedadesYCodigosAsociados() throws Exception {
		try {
			conectar();

			String id = "SELECT (disease.disease_id) "
					+ "FROM Disease;";
			Statement st =  conn.createStatement();
			ResultSet rs = st.executeQuery(id);

			while(rs.next()) {

				String id1 = rs.getString(1);
				String codeasociado   = "SELECT code " + "FROM Disease_has_code"
						+ "WHERE id= " + id1 + ";";
				Statement st1 = conn.createStatement();
				ResultSet rs1 = st1.executeQuery(codeasociado); //codes asociados al id

				String nombre   = "SELECT name " + "FROM Disease"
						+ "WHERE id= " + id1 + ";";
				Statement st2 = conn.createStatement();
				ResultSet rs2 = st2.executeQuery(nombre);

				while(rs1.next()) {
					String code1 = rs1.getString(1);
					String sourceasociado   = "SELECT source_id " + "FROM Code"
							+ "WHERE code= " + code1 + ";";
					Statement st3 = conn.createStatement();
					ResultSet rs3 = st3.executeQuery(sourceasociado); //source_id`s asociados a un code

					String sourceid = rs3.getString(1);
					String nombresource = "SELECT name " + "FROM Source"
							+ "WHERE source_id=" + sourceid + ";";
					Statement st4 = conn.createStatement();
					ResultSet rs4 = st4.executeQuery(nombresource);

					System.out.println(rs2.getObject(1) + "/" + rs1.getObject(1) + "," + rs4.getObject(1));
				}
			}
		} catch (SQLException ex) {
			System.err.println(ex);
		}

	}

	private void listarSintomasYTiposSemanticos() throws Exception { 
		try {
			conectar();

			String cuisymp = "SELECT (symptom.cui) "
					+ "FROM Symptom;";
			Statement st1 =  conn.createStatement();
			ResultSet rs1 = st1.executeQuery(cuisymp); //ids de los sintomas

			while(rs1.next()) {

				String nombre = "SELECT (symptom.name) "
						+ "FROM Symptom "+
						"WHERE cui= "+cuisymp+	";";
				Statement st =  conn.createStatement();
				ResultSet rs = st.executeQuery(nombre);//nombre sintomas asociado a id

				String cui1 = rs1.getString(1);
				String semanticasociado = "SELECT (symptom_semantic_type.semantic_type_id) "
						+ "FROM SymptomSemanticType "
						+ "WHERE cui= " +cui1+";";
				Statement st2 =  conn.createStatement();
				ResultSet rs2 = st2.executeQuery(semanticasociado); //semantics asociados a un cui

				System.out.println("Sintoma: " + rs.getObject(1) + " , Tipo Semantico: " + rs2.getObject(1) );
			}


		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	private void mostrarEstadisticasBD() throws Exception {
		try {
			conectar();

			String numEnfermedades= "SELECT (disease.disease_id)"
					+ "FROM Disease;";
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(numEnfermedades);//ids de enfermedades
			while(rs.next()) {
				int contador = 0;
				String numero = rs.getString(1);
				contador ++;
				System.out.println("El numero de enfermedades es: "+ contador);
			}

			String numSintomas= "SELECT (symptom.cui)"
					+ "FROM Symptom;";
			Statement st1 = conn.createStatement();
			ResultSet rs1 = st1.executeQuery(numSintomas);//ids de sintomas
			while(rs1.next()) {
				int contador = 0;
				String numero = rs1.getString(1);
				contador++;
				System.out.println("El numero de sintomas es: " + contador);
			}

			String maxSympEnf= "SELECT (disease.disease_id) "
					+ "FROM DiseaseSympton;";
			Statement st2 = conn.createStatement();
			ResultSet rs2 = st2.executeQuery(maxSympEnf);//ids de enfermedades
			while(rs2.next()) {
				String id = rs2.getString(1);
				String numcuiasociadoid = "SELECT COUNT (disease_symptom.cui) "
						+	"FROM DiseaseSymptom WHERE disease_id= " + id + ";";
				Statement st3 = conn.createStatement();
				ResultSet rs3 = st3.executeQuery(numcuiasociadoid);//num de cuis asociados a un id
				int comp = 0;
				while(rs3.next()) {
					int numcui = rs3.getInt(1);
					if(comp < numcui) {
						comp = numcui;
					}
				}
				String max = "SELECT (disase_symptom.disease_id)"
						+ "FROM DiseaseSymptom WHERE COUNT cui= "+ comp +";";
				Statement st4 = conn.createStatement();
				ResultSet rs4 = st4.executeQuery(max);

				int idfinal = rs4.getInt(1);

				String nombremaxENF = "SELECT (disase.name)"
						+ "FROM Disease WHERE disease.id= "+ idfinal +";";
				Statement st5 = conn.createStatement();
				ResultSet rs5 = st5.executeQuery(nombremaxENF);

				System.out.println("La enfermedad con mas sintomas es: "+ rs5.getObject(1));
			}

			String minSympEnf= "SELECT (disease.disease_id) "
					+ "FROM DiseaseSympton;";
			Statement st6 = conn.createStatement();
			ResultSet rs6 = st6.executeQuery(minSympEnf);//ids de enfermedades
			while(rs6.next()) {
				String id = rs6.getString(1);
				String numcuiasociadoid = "SELECT COUNT (disease_symptom.cui) "
						+	"FROM DiseaseSymptom WHERE disease_id= " + id + ";";
				Statement st7 = conn.createStatement();
				ResultSet rs7 = st7.executeQuery(numcuiasociadoid);//num de cuis asociados a un id

				int comp = rs7.getInt(1);
				while(rs7.next()) {
					int numcui = rs7.getInt(1);
					if(comp > numcui) {
						comp = numcui;
					}
				}
				String min = "SELECT (disase_symptom.disease_id)"
						+ "FROM DiseaseSymptom WHERE COUNT cui= "+ comp +";";
				Statement st8 = conn.createStatement();
				ResultSet rs8 = st8.executeQuery(min);

				int idfinal1 = rs8.getInt(1);

				String nombreminENF = "SELECT (disase.name)"
						+ "FROM Disease WHERE disease.id= "+ idfinal1 +";";
				Statement st9 = conn.createStatement();
				ResultSet rs9 = st9.executeQuery(nombreminENF);

				System.out.println("La enfermedad con menos sintomas es: "+ rs9.getObject(1));
			}
			
			String semTypes= "SELECT (semantic_type.semantic_type_id)"
					+ "FROM SemanticType";
			Statement  st10 = conn.createStatement();
			ResultSet  rs10 = st10.executeQuery(semTypes);//todos los ids semantic
			
			while(rs10.next()) {
				int semid = rs10.getInt(1);
				String numSym = "SELECT (symptom.cui) "
							+ "FROM SymptomSemanticType WHERE semantic_type_id= " + semid +";";
				Statement  st11 = conn.createStatement();
				ResultSet  rs11 = st11.executeQuery(numSym);//todos los sintomas asociados a un semantic
				
				String numSemTypes= "SELECT COUNT(semantic_type.semantic_type_id)"
						+ "FROM SemanticType";
				Statement  st12 = conn.createStatement();
				ResultSet  rs12 = st12.executeQuery(numSemTypes);//numero total de semantics
				
				System.out.println("Tipo Semantico: " + rs10.getObject(1) + ", Sintomas asociados: " + rs11.getObject(1));
				System.out.println("El numero total de Tipos Semanticos es: " + rs12.getObject(1));
			}
			int contador =0;
			int suma=0;
			int average=0;
			String avgsymp= "SELECT (disease.disease_id) "
					+ "FROM DiseaseSympton;";
			Statement st13 = conn.createStatement();
			ResultSet rs13 = st13.executeQuery(avgsymp);//ids de enfermedades
			while(rs13.next()) {
				String id = rs13.getString(1);
				String cuiavg = "SELECT COUNT (disease_symptom.cui) "
						+	"FROM DiseaseSymptom WHERE disease_id= " + id + ";";
				Statement st14 = conn.createStatement();
				ResultSet rs14 = st14.executeQuery(cuiavg);//num de cuis asociados a un id
				int sumador = rs14.getInt(1);
				contador++;
				suma=suma +sumador;
			}
			average=suma/contador;
			System.out.println("El numero medio de sintomas: "+average);
			
			
		}
		catch(SQLException ex){
			System.err.println(ex.getMessage());
		}
	}

	/**
	 * M�todo para leer n�meros enteros de teclado.
	 * 
	 * @return Devuelve el n�mero le�do.
	 * @throws Exception
	 *             Puede lanzar excepci�n.
	 */
	private int readInt() throws Exception {
		try {
			System.out.print("> ");
			return Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
		} catch (Exception e) {
			throw new Exception("Not number");
		}
	}

	/**
	 * M�todo para leer cadenas de teclado.
	 * 
	 * @return Devuelve la cadena le�da.
	 * @throws Exception
	 *             Puede lanzar excepci�n.
	 */
	private String readString() throws Exception {
		try {
			System.out.print("> ");
			return new BufferedReader(new InputStreamReader(System.in)).readLine();
		} catch (Exception e) {
			throw new Exception("Error reading line");
		}
	}

	/**
	 * M�todo para leer el fichero que contiene los datos.
	 * 
	 * @return Devuelve una lista de String con el contenido.
	 * @throws Exception
	 *             Puede lanzar excepci�n.
	 */
	private LinkedList<String> readData() throws Exception {
		LinkedList<String> data = new LinkedList<String>();
		BufferedReader bL = new BufferedReader(new FileReader(DATAFILE));
		while (bL.ready()) {
			data.add(bL.readLine());
		}
		bL.close();
		return data;
	}

	public static void main(String args[]) {
		new Diagnostico().showMenu();
	}
}

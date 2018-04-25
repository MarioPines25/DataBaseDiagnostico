
public class PruebaSplit {

	public static void main(String[] args) {
		String aDividir= "Varicela:23@Medline;84@Bddx;23@AlexGay=dolorDeCabeza:12:semantic1;dolorEstomago:13:semantic2";
		String[]enfSint;//array de enfermedades y sintomas



		String []enfermedades;
		String []codVoc;
		String []codigo;

		enfSint=aDividir.split("=",2);

		//COMIENZO PARTE IZQUIERDA ARBOL

		enfermedades=enfSint[0].split(":");
		codVoc=enfermedades[1].split(";");

		/*for(int i=0; i<enfermedades.length;i++){
			System.out.println(enfermedades[i]);

		}
		System.out.println("\n");

		for(int i=0; i<codVoc.length;i++){
			System.out.println(codVoc[i]);

		}
		System.out.println("\n");*/

		for(int i=0;i<codVoc.length;i++){
			//conseguimos codigos y vocabularios
			codigo=codVoc[i].split("@");

			System.out.println(codigo[0]);
			System.out.println(codigo[1]);

		}

		//FIN PARTE IZQUIERDA ARBOL

		//COMIENZO PARTE DERECHA ARBOL
		String [] sintomas;
		String [] elementos;
		//String [] datos;
		sintomas = enfSint[1].split(";");
		
		System.out.println("\n");
		System.out.println("\n");
		System.out.println("\n");
		
		for (int i=0; i<sintomas.length;i++){
			//System.out.println(sintomas[i]);
			elementos= sintomas[i].split(":");
			for (int j=0; j< elementos.length;j++){
				//datos= elementos[j].split(":");
				System.out.println(elementos[j]); 
			}
		}


	}
}

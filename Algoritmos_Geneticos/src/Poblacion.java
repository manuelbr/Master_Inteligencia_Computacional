import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import javafx.util.Pair;

public class Poblacion {
	private static int numGenes;
	private static  int tamanioPoblacion;
	private int[][] permutaciones;
	private int[][] permutacionesAprendidas;
	private int[] mejorPermutacion;
	private int[][] distancia;
	private int[][] pesos;
	private float menorPeso;
	private int generacionActual;
	private int generacionIdeal;
	private TipoAlgoritmo tA;
	private static ArrayList<Pair<Integer,Float>> fitness;
	
	public Poblacion(int nG, int tP, int[][] d, int[][] p, TipoAlgoritmo tpA){
		permutaciones = new int[tP][nG];
		mejorPermutacion = new int[nG];
		permutacionesAprendidas = new int[tP][nG];
		tamanioPoblacion = tP;
		numGenes = nG;
		distancia = d;
		pesos = p;
		menorPeso = 100000000000f;
		fitness = new ArrayList<Pair<Integer,Float>>(tamanioPoblacion);
		tA = tpA;
	}
	
	public void inicializa(){
		Random rand = new Random();
		int random;
		int aux;
		
		//Se inicializa la población con tamanioPoblación número de clones.
		for(int i = 0; i<tamanioPoblacion; i++){
			for(int j = 0; j<numGenes; j++){
				permutaciones[i][j] = (j+1);
			}
		}
		
		//Se alteran los clones para hacerlos diferenciados.
		for(int i = 0; i<tamanioPoblacion; i++){
			for(int k = 0; k<numGenes;k++){
				random = rand.nextInt((numGenes-1) + 1);
				while(random == k){
					random = rand.nextInt((numGenes-1) + 1);
				}
				aux = permutaciones[i][k];
				permutaciones[i][k] = permutaciones[i][random];
				permutaciones[i][random] = aux;
			} 
		}
		
		//Se calcula el fitness para la población inicial.
		for(int i = 0; i<tamanioPoblacion;i++){
			fitness.add(new Pair(i,calculaFitness(permutaciones[i])));
		}
	}
	
	
	public float calculaFitness(int[] individuo){
		float resultado = 0f;
		
		//Se recorren las matrices de peso y distancia que son n*n
		for(int i=0;i<pesos.length;i++){
			for(int j = 0; j<pesos.length;j++){
				resultado += pesos[i][j]*distancia[(individuo[i])-1][(individuo[j])-1];
			}
		}
		
		return resultado;
	}
	
	//Función de cálculo de influencia de pares de genes para un individuo concreto.
	//Para no tener que calcular el fitness cada vez, sino sólamente ajustarlo cuando aplicamos 2-opt.
	public float calculaCosteGen(int[] individuo, int i, int j){//Gen 1 = i, Gen 2 = j
		float resultado = 0f;
		
		for(int k = 0; k<numGenes;k++){
	         resultado += pesos[i][k]*distancia[individuo[i]-1][individuo[k]-1];
	         resultado += pesos[j][k]*distancia[individuo[j]-1][individuo[k]-1];

	         if (i!=k && j!=k) {
	        	 resultado += pesos[k][i]*distancia[individuo[k]-1][individuo[i]-1];
		         resultado += pesos[k][j]*distancia[individuo[k]-1][individuo[j]-1];
	         }
		}
		return resultado;
	}
	
	//Función de optimización 2-opt.
	public void aprender(){
		int aux;
		int aux2;
		int i;
		int[] individuo = new int[numGenes];
		float mejorFitness;
		float original;
		float costeGenAnt;
		float costeGenDes;
		int auxiliar;
		int index_mejorFitness;
		
		//La copia se hace a mano para evitar cambiar posiciones de memoria equivocadas.
		for(int l = 0; l<permutaciones.length; l++){
			for(int j = 0; j<permutaciones[l].length;j++){
				auxiliar = permutaciones[l][j];
				permutacionesAprendidas[l][j] = auxiliar;
			}
		}
		
		for(int j = 0; j<(fitness.size()-5);j++){
			individuo = permutacionesAprendidas[j];
			original = calculaFitness(individuo);
			mejorFitness = original;

			for(i=0;i<numGenes;i++){
				index_mejorFitness = i;
				for(int l = (i+1); l <numGenes; l++){//Se hacen numGenes intercambios
					//Se calcula el coste del gen antes del intercambio
					costeGenAnt = calculaCosteGen(individuo,i,l);
					
					//Se produce el intercambio de genes en el individuo
					aux = individuo[i];
					aux2 = individuo[l];
					individuo[i] = aux2;
					individuo[l] = aux;
					////////////////////////////////////////////////////
					
					//Se calcula el coste del gen después del intercambio
					costeGenDes = calculaCosteGen(individuo,i,l);
				
					//Se deja al individuo como estaba antes del cambio
					aux = individuo[i];
					aux2 = individuo[l];
					individuo[i] = aux2;
					individuo[l] = aux;

					//Si se vé que el intercambio de genes produce mejores soluciones
					//Se tiene en cuenta esa mejor solución y se sigue comprobando si se puede hacer una 
					//mejor solución.
					if((original-costeGenAnt+costeGenDes)<mejorFitness){
						mejorFitness = (original-costeGenAnt+costeGenDes);
						index_mejorFitness = l;
					}
				}
				//Se deja al individuo en su forma de mejor fitness para la posición i.
				aux = individuo[i];
				aux2 = individuo[index_mejorFitness];
				individuo[i] = aux2;
				individuo[index_mejorFitness] = aux;
				original = mejorFitness;
			}
			fitness.set(j,(new Pair(j, mejorFitness)));
			//Se copia manual para evitar copias indeseadas de posiciones de memoria
			for(int m = 0; m<individuo.length;m++){
				aux = individuo[m];
				permutacionesAprendidas[j][m] = aux; 
			}
		}
	}
	
	//La función evalua obtiene el fitness de cada individuo.
	public void evalua(){
		int auxiliar;
		
		switch(tA){
			case Baldwin: aprender();
						break;
			case Lamarck: aprender();
					for(int i = 0; i<permutacionesAprendidas.length; i++){
						for(int j = 0; j<permutacionesAprendidas[i].length;j++){
							auxiliar = permutacionesAprendidas[i][j];
							permutaciones[i][j] = auxiliar;
						}
					}
					break;
			default: fitness.clear(); 
					 for(int k=0;k<tamanioPoblacion; k++){
						fitness.add(new Pair(k, calculaFitness(permutaciones[k])));
					 }
					break;
		}
	}
	
	//Función para ordenar el arraylist de fitness, en función del valor.
	public ArrayList<Pair<Integer,Float>> ordenaFitness(ArrayList<Pair<Integer,Float>> ft){
		Collections.sort(ft, new Comparator<Pair<Integer, Float>>() {
		    @Override
		    public int compare(final Pair<Integer, Float> o1, final Pair<Integer, Float> o2) {
		       if(o1.getValue()<o2.getValue()){
		        	return 1;
		       }else
			     	return -1;
			   }
		});
		
		return ft;
	}
	
	//Selecciona los dos individuos con mejores resultados
	public ArrayList<Pair<Integer,Integer>> selecciona(int generacion){
		generacionActual = generacion;
		int auxiliar;
		int contendiente;
		ArrayList<Pair<Integer,Float>> contendientes = new ArrayList<Pair<Integer,Float>>();
		ArrayList<Pair<Integer,Integer>>  padres = new ArrayList<Pair<Integer,Integer>>();
		Pair<Integer,Integer> pareja = null;

			
		//Se ordena el fitness
		fitness = ordenaFitness(fitness);
		
		Random rand = new Random();
		
		//Se descubre a los padres
		for(int k = 0; k<(fitness.size()/2);k++){
			do{
				contendientes.clear();
				for(int i=0; i<10;i++){
					contendiente = rand.nextInt((tamanioPoblacion-1) + 1);
					
					while(contendientes.contains(fitness.get(contendiente))){
						contendiente = rand.nextInt((tamanioPoblacion-1) + 1);
					}
					contendientes.add(fitness.get(contendiente));
				}
				contendientes = ordenaFitness(contendientes);
				pareja = new Pair(contendientes.get(contendientes.size()-1).getKey(),contendientes.get(contendientes.size()-2).getKey());
			}while(padres.contains(pareja));
			padres.add(pareja);
		}
		
		if(fitness.get(fitness.size()-1).getValue()<menorPeso){
			menorPeso = fitness.get(fitness.size()-1).getValue();
			generacionIdeal = generacionActual;
			
			//Se copia de forma manual para evitar cambios de valor en posiciones de memoria no deseados.
			for(int i = 0; i<permutaciones[fitness.get(fitness.size()-1).getKey()].length; i++){
				auxiliar = permutaciones[fitness.get(fitness.size()-1).getKey()][i];
				mejorPermutacion[i] = auxiliar;
			}
		}
		
		System.out.println("En la generación: "+generacion+" se ha obtenido el valor: "+fitness.get(fitness.size()-1).getValue()+" el menor peso hasta ahora es: "+menorPeso);
		
		return padres;
	}
	
	//Función auxiliar
	public ArrayList<Integer> inicializaNiños(ArrayList<Integer> ninio, int tamanio){
		ninio.clear();
		
		for(int i = 0; i<tamanio;i++){
			ninio.add(0);
		}
		
		return ninio;
	}
	
	//Función del operador de cruce: cruce de orden
	public void cruza(ArrayList<Pair<Integer,Integer>> padres){
		ArrayList<Integer> niño = new ArrayList<Integer>(numGenes);
		ArrayList<Integer> niña = new ArrayList<Integer>(numGenes);
		int[][] poblacioModificada = new int[tamanioPoblacion][numGenes];
		
		int[] padre,madre;
		int numero;
		int corte;
		int aux,aux2;
		int l = -1;
		int indice_hijo = 0;
		
		Random rand = new Random();
		
		for(int i = 0; i<(padres.size()-5);i++){
			
			int[] ninio = new int[numGenes];
			int[] ninia = new int[numGenes];
				
			padre = permutaciones[padres.get(i).getKey()];
			madre = permutaciones[padres.get(i).getValue()];
			
			niño = inicializaNiños(niño,numGenes);
			niña = inicializaNiños(niña,numGenes);
			
			numero = numGenes/2;
			corte = rand.nextInt((numGenes-numero-1) + 1);
			
			//Se copia el trozo representativo del código genético del padre/madre. 
			for(int k = corte; k<(corte+numero); k++){
				aux = padre[k]; 
				aux2 = madre[k];
				niño.set(k,aux);
				niña.set(k,aux2);
				l = k;
			}
		
			l++; //Se actualiza a la siguiente posición del k último guardado.
			int p = l;
			int s = l;
			while((l%numGenes) != corte){
				aux = madre[p%numGenes]; 
				aux2 = padre[s%numGenes];
				
				while(niño.contains(aux)){
					p++;
					aux = madre[p%numGenes];
				}
				
				while(niña.contains(aux2)){
					s++;
					aux2 = padre[s%numGenes];
				}
				
				niño.set(l%numGenes,aux);
				niña.set(l%numGenes,aux2);
				l++;
				p++;
				s++;
			}
			
			//Se parsea a array normal (no se hace usando toArray, porque da problemas al pasar entre Integer e int)
			for(int m = 0; m<niño.size(); m++){
				ninio[m] = niño.get(m);
				ninia[m] = niña.get(m);
			}
	
			poblacioModificada[fitness.get(indice_hijo).getKey()] = ninia;
			poblacioModificada[fitness.get(indice_hijo+1).getKey()] = ninio;
			
			indice_hijo +=2;
		}
		
		//Sobreviven los 5 mejores individuos de la población
		for(int m = 0; m<5; m++){
			int [] aux3 = permutaciones[fitness.get(indice_hijo).getKey()];
			int [] aux4 = permutaciones[fitness.get(indice_hijo+1).getKey()];
			poblacioModificada[fitness.get(indice_hijo).getKey()] = aux3;
			poblacioModificada[fitness.get(indice_hijo+1).getKey()] = aux4;
			
			indice_hijo +=2;
		}
		
		//Se sustituye la vieja población por la nueva
		for(int i =0; i<tamanioPoblacion;i++){
			for(int k = 0; k<numGenes;k++){
				aux = poblacioModificada[i][k];
				permutaciones[i][k] = aux;
			}
		}
	}
	
	//Función del operador de mutación estándar.
	public void mutar(){
		Random rand = new Random();
		int random;
		int aux,aux2;
		
		//Se alteran los clones para hacerlos diferenciados.
		for(int i = 0; i<(fitness.size()-5); i++){
			for(int k = 0; k<numGenes;k++){
				//Se comprueba si se va a mutar o no.
				random = rand.nextInt(((numGenes-1) - 0) + 1) + 0;
				if(random == k){//Enorabuena gen, vas a mutar
					//Proceso de mutación (Se intercambia su posición con otro del individuo)
					random = rand.nextInt(((numGenes-1) - 0) + 1) + 0;
					while(random == k){
						random = rand.nextInt(((numGenes-1) - 0) + 1) + 0;
					}
					aux = permutaciones[fitness.get(i).getKey()][k];
					aux2 = permutaciones[fitness.get(i).getKey()][random];
					permutaciones[fitness.get(i).getKey()][k] = aux2;
					permutaciones[fitness.get(i).getKey()][random] = aux;
				}
			} 
		}
		
		
	}
	
	public float getMenorPeso(){
		return menorPeso;
	}
	
	public int getGeneracionIdeal(){
		return generacionIdeal;
	}
	
	public int[] getMejorPermutacion(){
		return mejorPermutacion;
	}
}

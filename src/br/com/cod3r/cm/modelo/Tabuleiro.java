package br.com.cod3r.cm.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Tabuleiro implements CampoObservador {

	private int linhas;
	private int colunas;
	private int minas;
	
	private final List<Campo> campos = new ArrayList<>();
	private final List<Consumer<ResultadoEvento>> observadores =
			new ArrayList<>();

	public Tabuleiro(int linhas, int colunas, int minas) {
		this.linhas = linhas;
		this.colunas = colunas;
		this.minas = minas;
	
		gerarCampos();
		associarVizinhos();
		sortearMinas();
	}
	
	public void paraCadaCampo(Consumer<Campo> funcao) {
		campos.forEach(funcao);
	}
	
	public void registrarObservador(Consumer<ResultadoEvento> obserador) {
		observadores.add(obserador);
	}
	
	public void notificarObservador(boolean resultado) {
		observadores.stream()
			.forEach(o -> o.accept(new ResultadoEvento(resultado)));
	}
	
	public void abrir(int linha, int coluna) {
		campos.parallelStream()
			.filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
			.findFirst()
			.ifPresent(c -> c.abrir());
	}
	
	public void alterarMarcacao(int linha, int coluna) {
		campos.parallelStream()
			.filter(c -> c.getLinha() == linha && c.getColuna() == coluna)
			.findFirst()
			.ifPresent(c -> c.altenarMarcacao());
	}

	private void gerarCampos() {
		for (int linha = 0; linha < linhas; linha++) {
			for (int coluna = 0; coluna < colunas; coluna++) {
				Campo campo = new Campo(linha, coluna);
				campo.registrarObservador(this);
				campos.add(campo);
			}
		}
	}
		
	private void associarVizinhos() {
		for(Campo c1: campos) {
			for(Campo c2: campos) {
				c1.adicionarVizinho(c2);
			}
		}
	}
	
	private void sortearMinas() {
		long minasArmadas = 0;
		Predicate<Campo> minado = c -> c.isMinado();
		
		do {
			int aleatorio = (int) (Math.random() * campos.size());
			campos.get(aleatorio).minar();
			minasArmadas = campos.stream().filter(minado).count();
		} while(minasArmadas < minas);
	}
	
		public boolean objetivoAlcancado() {
			return campos.stream().allMatch(c -> c.objetivoAlcancado());
		}
		
		public void reniciar() {
			campos.stream().forEach(c -> c.reniciar());
			sortearMinas();
		}

		public int getLinhas() {
			return linhas;
		}

		public void setLinhas(int linhas) {
			this.linhas = linhas;
		}

		public int getColunas() {
			return colunas;
		}

		public void setColunas(int colunas) {
			this.colunas = colunas;
		}

		public void eventoOcorreu(Campo campo, CampoEvento evento) {
			if (evento == CampoEvento.EXPLODIR) {
				mostraMinas();
				notificarObservador(false);
			} else if (objetivoAlcancado()) {
				notificarObservador(true);
			}
		}
		
		private void mostraMinas() {
			campos.stream()
			.filter(c -> c.isMinado())
			.filter(c -> !c.isMarcado())
			.forEach(c -> c.setAberto(true)); 
		}
		
	}


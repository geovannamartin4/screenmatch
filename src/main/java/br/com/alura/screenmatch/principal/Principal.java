package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    Scanner sc = new Scanner(System.in);

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=5cd120e1";
    private  ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();
    private Optional<Serie> serieBuscada;

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por titulo  
                    5 - Buscar série por ator
                    6 - Listar top 5 séries
                    7 - Buscar categorias
                    8 - Listar por Temporada e Avaliação
                    9 - Buscar episodio por trecho
                    10 - Top Episodios por Serie
                    11 - Buscar episodios por data de lançamento
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = sc.nextInt();
            sc.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriePorAtor();
                    break;
                case 6:
                    listarTopSerie();
                    break;
                case 7:
                    listarPorCategoria();
                    break;
                case 8:
                    listarPorTemporadaAvaliacao();
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 11:
                    buscarEpisodiosPorDataDeLancamento();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = sc.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Digite o episodio que deseja buscar: ");
        var nomeSerie = sc.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();
            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

           List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                    .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
           serieEncontrada.setEpisodios(episodios);
           repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada");
        }

    }

    private void listarSeriesBuscadas(){
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriePorTitulo() {
        System.out.println("Digite o episodio que deseja buscar: ");
        var nomeSerie = sc.nextLine();
        serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if(serieBuscada.isPresent()) {
            System.out.println("Dados série: " + serieBuscada.get());
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarSeriePorAtor() {
        System.out.println("Digite o ator que deseja buscar: ");
        var nomeAtor = sc.nextLine();
        System.out.println("Digite a partir de qual nota deseja buscar: ");
        Double avaliacao = sc.nextDouble();

        List<Serie> atorBuscado = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);

        System.out.println("Series em que" + nomeAtor + "trabalhou");
        atorBuscado.forEach(a -> System.out.println(a.getTitulo() + a.getAvaliacao()));
    }

    private void listarTopSerie() {
        List<Serie> top5 = repositorio.findTop5ByOrderByAvaliacaoDesc();
        System.out.println(top5);
    }

    private void  listarPorCategoria() {
        System.out.println("Digite a categoria para buscar: ");
        String nomeGenero = sc.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> serieBuscada = repositorio.findByGenero(categoria);

        serieBuscada.forEach(System.out::println);
    }

    private void listarPorTemporadaAvaliacao() {
        System.out.println("Digite o total de temporadas que deseja: ");
        var totalTemporada = sc.nextInt();
        System.out.println("Digite a partir de qual avaliacao deseja: ");
        var avaliacao = sc.nextDouble();

        List<Serie> resultado = repositorio.seriePorTemporadaAvaliacao(totalTemporada, avaliacao);

        resultado.forEach(System.out::println);
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Digite o trecho para busca: ");
        var trechoEpisodio = sc.nextLine();
        List<Episodio> episodioBuscada = repositorio.episodiosPorTrecho(trechoEpisodio);
        episodioBuscada.forEach(e-> System.out.println(e.getSerie() + e.getTitulo() + e.getTemporada()
                + e.getNumeroEpisodio() + e.getTitulo()));
    }

    private void topEpisodiosPorSerie() {
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            List<Episodio> episodioBuscado = repositorio.top5EpisodiosPorSerie(serie);
            episodioBuscado.forEach(e-> System.out.println(e.getSerie() + e.getTitulo() + e.getTemporada() +
                    e.getNumeroEpisodio() + e.getTitulo() + e.getAvaliacao()));
        }

    }

    private void buscarEpisodiosPorDataDeLancamento() {
        buscarSeriePorTitulo();
        if (serieBuscada.isPresent()) {
            Serie serie = serieBuscada.get();
            System.out.println("Digite o ano limite de lançamento: ");
            var anoLancamento = sc.nextInt();
            sc.nextLine();
            List<Episodio> episodioBuscado = repositorio.buscaEpisodioPorDataDeLancamento(anoLancamento, serie);
            episodioBuscado.forEach(System.out::println);
        }

    }


}

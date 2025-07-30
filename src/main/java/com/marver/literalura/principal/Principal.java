package com.marver.literalura.principal;

import com.marver.literalura.model.*;
import com.marver.literalura.Service.ConsumoAPI;
import com.marver.literalura.Service.ConvierteDatos;
import com.marver.literalura.model.DatosBusqueda;
import com.marver.literalura.repository.AutorRepository;
import com.marver.literalura.repository.AutorRepository;

import java.util.*;
import java.util.IntSummaryStatistics;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private String URL_BASE = "https://gutendex.com/books/";
    private AutorRepository repository;

    public Principal(AutorRepository repository){
        this.repository = repository;
    }



    public void muestraMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
          
               Bienvenidos a la biblioteca LITERALURA
            --------------------------------------------
            1 - Buscar Libros por TÍtulo
            2 - Listar Libros Registrados
            3 - Listar Autores Registrados
            4 - Listar Autores Vivos en un determinado año
            5 - Listar Libros por Idioma           
            6 - Generar Estadísticas 
            7 - Top 10 Libros más Descargados
            8 - Buscar Autor por Nombre                               
            0 - Salir del programa
            ----------------------------------------------
            Elija una opcion...
            
            """;


            System.out.println(menu);
            try {
                opcion = Integer.valueOf(teclado.nextLine());
                switch (opcion) {
                    case 1:
                        buscarLibroPorTitulo();
                        break;
                    case 2:
                        listarLibrosRegistrados();
                        break;
                    case 3:
                        listarAutoresRegistrados();
                        break;
                    case 4:
                        listarAutoresVivos();
                        break;
                    case 5:
                        listarLibrosPorIdioma();
                        break;
                    case 6:
                        generarEstadisticas();
                        break;
                    case 7:
                        top10LibrosDescargados();
                        break;
                    case 8:
                        buscarAutorPorNombre();
                        break;
                    case 0:
                        System.out.println("Cerrando la aplicacion...");
                        break;
                    default:
                        System.out.println("Opción inválida!!! Ingrese una opcion del Menu!!!");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Opción inválida: " + e.getMessage());

            }
        }
    }

    public void buscarLibroPorTitulo() {
        System.out.println("""
           
             1 - Buscar libro por titulo
            ==============================
             """);
        System.out.println("Ingrese el nombre del libro que deseas buscar: ");
        var nombre = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombre.replace(" ", "+").toLowerCase());


        if (json.isEmpty() || !json.contains("\"count\":0,\"next\":null,\"previous\":null,\"results\":[]")) {
            var datos = conversor.obtenerDatos(json, DatosBusqueda.class);


            Optional<DatosLibro> libroBuscado = datos.libros().stream()
                    .findFirst();
            if (libroBuscado.isPresent()) {
                System.out.println("* Libro buscado: " +
                        "\n........................................................................................." +
                        "\n -> Título: " + libroBuscado.get().titulo() +
                        "\n -> Autor: " + libroBuscado.get().autores().stream()
                        .map(a -> a.nombre()).limit(1).collect(Collectors.joining()) +
                        "\n -> Idioma: " + libroBuscado.get().idiomas().stream().collect(Collectors.joining()) +
                        "\n -> Número de descargas: " + libroBuscado.get().descargas() +
                        "\n.........................................................................................\n"
                );

                try {
                    List<Libro> libroEncontrado = libroBuscado.stream().map(a -> new Libro(a)).collect(Collectors.toList());
                    Autor autorAPI = libroBuscado.stream().
                            flatMap(l -> l.autores().stream()
                                    .map(a -> new Autor(a)))
                            .collect(Collectors.toList()).stream().findFirst().get();
                    Optional<Autor> autorBD = repository.buscarAutorPorNombre(libroBuscado.get().autores().stream()
                            .map(a -> a.nombre())
                            .collect(Collectors.joining()));
                    Optional<Libro> libroOptional = repository.buscarLibroPorNombre(nombre);
                    if (libroOptional.isPresent()) {
                        System.out.println("El libro ya está guardado");
                    } else {
                        Autor autor;
                        if (autorBD.isPresent()) {
                            autor = autorBD.get();
                            System.out.println("EL autor ya esta guardado");
                        } else {
                            autor = autorAPI;
                            repository.save(autor);
                        }
                        autor.setLibros(libroEncontrado);
                        repository.save(autor);
                    }
                } catch (Exception e) {
                    System.out.println("!" + e.getMessage());
                }
            } else {
                System.out.println("Libro no encontrado!!!");
            }
        }
    }


    public void listarLibrosRegistrados () {
        System.out.println("""
                    2 - Listar libros registrados
                    ================================
                     """);
        List<Libro> libros = repository.buscarTodosLosLibros();
        libros.forEach(l -> System.out.println(
                "\n............................................." +
                        "\n -> Título: " + l.getTitulo() +
                        "\n -> Autor: " + l.getAutor().getNombre() +
                        "\n -> Idioma: " + l.getIdioma().getIdioma() +
                        "\n -> Número de descargas: " + l.getDescargas() +
                        "\n.............................................\n"
        ));
    }

    public void listarAutoresRegistrados () {
        System.out.println("""
                    3 - Listar autores registrados
                    ================================
                     """);
        List<Autor> autores = repository.findAll();
        System.out.println("Autores Registrados: ");
        autores.forEach(l -> System.out.println(
                "\n -> Autor: " + l.getNombre() +
                        "\n -> Fecha de Nacimiento: " + l.getNacimiento() +
                        "\n -> Fecha de Fallecimiento: " + l.getFallecimiento() +
                        "\n -> Libros: " + l.getLibros().stream()
                        .map(t -> t.getTitulo()).collect(Collectors.toList()) + "\n"
        ));
    }

    public void listarAutoresVivos () {
        System.out.println("""
                    4 - Listar Autores Vivos en un determinado año
                    ================================================
                     """);
        System.out.println("Ingrese un año para verificar si el autor esta vivo: ");
        try {
            var fecha = Integer.valueOf(teclado.nextLine());
            List<Autor> autores = repository.buscarAutoresVivos(fecha);
            if (!autores.isEmpty()) {
                System.out.println("Autores vivos:");
                autores.forEach(a -> System.out.println(
                        "\n -> Autor: " + a.getNombre() +
                                "\n -> Fecha de Nacimiento: " + a.getNacimiento() +
                                "\n -> Fecha de Fallecimiento: " + a.getFallecimiento() +
                                "\n -> Libros: " + a.getLibros().stream()
                                .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
                ));
            } else {
                System.out.println("No hay autores vivos en el año registrado");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ingresa un año válido " + e.getMessage());
        }
    }

    public void listarLibrosPorIdioma() {
        System.out.println("""
                5 - Listar Libros por Idioma
                =============================
                """);
        var menu = """
                    Seleccione una opcion de idioma
                    ................................
                    1 - Español - es
                    2 - Inglés - en
                    3 - Frances - fr
                    """;
        System.out.println(menu);

        try {
            var opcion = Integer.parseInt(teclado.nextLine());

            switch (opcion) {
                case 1:
                    buscarLibrosPorIdioma("es");
                    break;
                case 2:
                    buscarLibrosPorIdioma("en");
                    break;
                case 3:
                    buscarLibrosPorIdioma("fr");
                    break;
                default:
                    System.out.println("Opción inválida!!!");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Opción inválida: " + e.getMessage());
        }
    }



    private void buscarLibrosPorIdioma(String idioma) {

        try {
            Idioma idiomaEnum = Idioma.valueOf(idioma.toUpperCase());
            List<Libro> libros = repository.buscarLibrosPorIdioma(idiomaEnum);
            if (libros.isEmpty()) {
                System.out.println("No hay libros registrados en ese idioma");
            } else {
                System.out.println("Libros registrados en el idioma buscado: ");
                libros.forEach(l -> System.out.println(
                        "\n.............................................." +
                                "\n -> Título: " + l.getTitulo() +
                                "\n -> Autor: " + l.getAutor().getNombre() +
                                "\n -> Idioma: " + l.getIdioma().getIdioma() +
                                "\n -> Número de descargas: " + l.getDescargas() +
                                "\n..............................................\n"
                ));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Ingrese una opcion valida.");
        }
    }

    public void generarEstadisticas () {
        System.out.println("""
                    6 - Generar Estadísticas
                    =========================
                     """);
        var json = consumoAPI.obtenerDatos(URL_BASE);
        var datos = conversor.obtenerDatos(json, DatosBusqueda.class);
        IntSummaryStatistics est = datos.libros().stream()
                .filter(l -> l.descargas() > 0)
                .collect(Collectors.summarizingInt(DatosLibro::descargas));
        Integer media = (int) est.getAverage();
        System.out.println("Media de descargas: " + media);
        System.out.println("Mínima de descargas: " + est.getMin());
        System.out.println("Máxima de descargas: " + est.getMax());

        System.out.println("Total de registros: " + est.getCount());

    }


    public void top10LibrosDescargados() {
        System.out.println("""
                    7 - Top 10 Libros más Descargados
                    ===============================
                     """);
        List<Libro> libros = repository.top10Libros();
        //System.out.println();
        libros.forEach(l -> System.out.println(
                "\n............................................." +
                        "\n -> Título: " + l.getTitulo() +
                        "\n -> Autor: " + l.getAutor().getNombre() +
                        "\n -> Idioma: " + l.getIdioma().getIdioma() +
                        "\n -> Número de descargas: " + l.getDescargas() +
                        "\n............................................."
        ));
    }


    public void buscarAutorPorNombre () {
        System.out.println("""
                    
                     8 - Buscar autor por nombre
                    ==============================
                    """);
        System.out.println("Ingrese el nombre del autor que deseas buscar:");
        var nombre = teclado.nextLine();
        Optional<Autor> autor = repository.buscarAutorPorNombre(nombre);
        if (autor.isPresent()) {
            System.out.println(
                    "\n -> Autor: " + autor.get().getNombre() +
                            "\n -> Fecha de Nacimiento: " + autor.get().getNacimiento() +
                            "\n -> Fecha de Fallecimiento: " + autor.get().getFallecimiento() +
                            "\n -> Libros: " + autor.get().getLibros().stream()
                            .map(l -> l.getTitulo()).collect(Collectors.toList()) + "\n"
            );
        } else {
            System.out.println("El autor no existe");
        }
    }


}

package com.marver.literalura.repository;

import com.marver.literalura.model.Autor;
import com.marver.literalura.model.Idioma;
import com.marver.literalura.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {


    //Creamos el metodo para buscar libros por su nombre
    @Query("SELECT l FROM Libro l JOIN l.autor a WHERE l.titulo LIKE %:nombre%")
    Optional<Libro> buscarLibroPorNombre(@Param("nombre") String nombre);

    //Creamos el metodo para mostrar autores vivos a partir de una año determinado
    @Query("SELECT a FROM Autor a WHERE a.fallecimiento > :fecha")
    List<Autor> buscarAutoresVivos(@Param("fecha") Integer fecha);

    //Creamos el metodo para mostrar los libros en su idiama
    @Query("SELECT l FROM Autor a JOIN a.libros l WHERE l.idioma = :idioma")
    List<Libro> buscarLibrosPorIdioma(@Param("idioma") Idioma idioma);

    //Creamoa el metodo para mostrar los 10 mejores libros descargados
    @Query("SELECT l FROM Autor a JOIN a.libros l ORDER BY l.descargas DESC LIMIT 10")
    List<Libro> top10Libros();

    //Creamos el metodo para mostrar todos los libros de la bd usando jpql
    @Query("SELECT l FROM Autor a JOIN a.libros l")
    List<Libro> buscarTodosLosLibros();

    //Creamos el metodo para buscar a los autores por su nombre
    @Query("SELECT a FROM Libro l JOIN l.autor a WHERE a.nombre LIKE %:nombre%")
    Optional<Autor> buscarAutorPorNombre(@Param("nombre") String nombre);
}

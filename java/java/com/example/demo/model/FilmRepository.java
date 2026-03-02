package com.example.demo.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilmRepository extends JpaRepository<Film, Integer> {

    
    // ESTENDIAMO LA CLASSE PER OTTENERE I METODI PER COMUNICARE CON IL DATABASE 
}


/**
 * Interfaccia Repository per la gestione delle operazioni CRUD sulla tabella.
 * Estendendo JpaRepository, eredita automaticamente metodi come save(), findAll(), findById(), delete().
 */
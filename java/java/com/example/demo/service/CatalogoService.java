package com.example.demo.service;


import com.example.demo.model.Film;
import com.example.demo.model.FilmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogoService {

    @Autowired
    private FilmRepository filmRepository;

    /**
     * Recupera TUTTI i film presenti nel database, senza applicare filtri.
     * Questa funzione serve a raccogliere i dati grezzi dal DB.
     * @return Una lista completa di tutti gli oggetti Film.
     */
    public List<Film> getAllFilms() {
        
        System.out.println("SERVICE: Raccogliendo tutti i film dal database.");
        
        // Chiama direttamente il metodo findAll() di JpaRepository.
        // Questa chiamata esegue SELECT * FROM film in MySQL.
        return filmRepository.findAll();
    }
    
} 

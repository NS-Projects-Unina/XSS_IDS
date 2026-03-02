package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.demo.model.Film;
import com.example.demo.model.FilmRepository;

@Service
public class FilmService {

    @Autowired
    private FilmRepository filmRepository;

    public Film getFilmById(int id) {
        return filmRepository.findById(id).orElse(null);
    }

    public Film saveFilm(@RequestBody Film film) {
        return filmRepository.save(film);
    }

    public void deleteFilmById(int id) {
    filmRepository.deleteById(id);
}
}


package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; 


@JsonPropertyOrder({ "id", "titolo", "durata_min", "descrizione" })
// L'annotazione @Entity marca questa classe come un'entità JPA
@Entity
// L'annotazione @Table (opzionale) specifica il nome della tabella nel DB
@Table(name = "catalogo") 
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // CAMPI DATI PRINCIPALI

    private String titolo;
    private String durata_min;
    private String descrizione;
    private Double noleggio;
    private Double acquisto;
    private int recensione;

    // Costruttore senza argomenti (richiesto da JPA/Hibernate)
    public Film() {
    }

    // Costruttore con argomenti per comodità
    public Film(int id,String titolo, String Durata, String Descrizione, Double Noleggio, Double Acquisto,int rec) {
        this.id=id;
        this.titolo = titolo;
        this.durata_min=Durata;
        this.descrizione=Descrizione;
        this.noleggio=Noleggio;
        this.acquisto=Acquisto;
        this.recensione=rec;
    }
    
    public int getId() {
        return id;
    }


    public String getTitolo() {
        return titolo;
    }

    public String getDurata_min() {
        return this.durata_min;
    }

    public String getDescrizione() {
        return this.descrizione;
    }
    public Double getNoleggio() {
        return this.noleggio;
    }
    public Double getAcquisto() {
        return this.acquisto;
    }

        public int getRecensione() {
        return this.recensione;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public void setDurata_min(String durata_min) {
        this.durata_min = durata_min;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public void setNoleggio(Double noleggio) {
        this.noleggio = noleggio;
    }

    public void setAcquisto(Double acquisto) {
        this.acquisto = acquisto;
    }

    public void setRecensione(int rec) {
        this.recensione = rec;
    }
} 
    

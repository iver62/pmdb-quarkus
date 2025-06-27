CREATE VIEW film_personne_vue AS
SELECT fk_film,
    fk_personne
   FROM ( SELECT lnk_film_acteur.fk_acteur AS fk_personne,
            lnk_film_acteur.fk_film
           FROM lnk_film_acteur
        UNION
		   SELECT lnk_film_artiste.fk_personne,
            lnk_film_artiste.fk_film
           FROM lnk_film_artiste
        UNION
         SELECT lnk_film_assistant_realisateur.fk_personne,
            lnk_film_assistant_realisateur.fk_film
           FROM lnk_film_assistant_realisateur
        UNION
         SELECT lnk_film_cascadeur.fk_personne,
            lnk_film_cascadeur.fk_film
           FROM lnk_film_cascadeur
        UNION
         SELECT lnk_film_casteur.fk_personne,
            lnk_film_casteur.fk_film
           FROM lnk_film_casteur
        UNION
         SELECT lnk_film_coiffeur.fk_personne,
            lnk_film_coiffeur.fk_film
           FROM lnk_film_coiffeur
        UNION
         SELECT lnk_film_compositeur.fk_personne,
            lnk_film_compositeur.fk_film
           FROM lnk_film_compositeur
        UNION
         SELECT lnk_film_costumier.fk_personne,
            lnk_film_costumier.fk_film
           FROM lnk_film_costumier
        UNION
         SELECT lnk_film_decorateur.fk_personne,
            lnk_film_decorateur.fk_film
           FROM lnk_film_decorateur
        UNION
         SELECT lnk_film_ingenieur_son.fk_personne,
            lnk_film_ingenieur_son.fk_film
           FROM lnk_film_ingenieur_son
        UNION
         SELECT lnk_film_maquilleur.fk_personne,
            lnk_film_maquilleur.fk_film
           FROM lnk_film_maquilleur
        UNION
         SELECT lnk_film_monteur.fk_personne,
            lnk_film_monteur.fk_film
           FROM lnk_film_monteur
        UNION
         SELECT lnk_film_musicien.fk_personne,
            lnk_film_musicien.fk_film
           FROM lnk_film_musicien
        UNION
         SELECT lnk_film_photographe.fk_personne,
            lnk_film_photographe.fk_film
           FROM lnk_film_photographe
        UNION
         SELECT lnk_film_producteur.fk_personne,
            lnk_film_producteur.fk_film
           FROM lnk_film_producteur
        UNION
         SELECT lnk_film_realisateur.fk_personne,
            lnk_film_realisateur.fk_film
           FROM lnk_film_realisateur
        UNION
         SELECT lnk_film_scenariste.fk_personne,
            lnk_film_scenariste.fk_film
           FROM lnk_film_scenariste
        UNION
         SELECT lnk_film_specialiste_effets_speciaux.fk_personne,
            lnk_film_specialiste_effets_speciaux.fk_film
           FROM lnk_film_specialiste_effets_speciaux
		UNION
         SELECT lnk_film_specialiste_effets_visuels.fk_personne,
            lnk_film_specialiste_effets_visuels.fk_film
           FROM lnk_film_specialiste_effets_visuels
	 ) all_movies
  GROUP BY fk_film, fk_personne
  ORDER BY fk_film, fk_personne;
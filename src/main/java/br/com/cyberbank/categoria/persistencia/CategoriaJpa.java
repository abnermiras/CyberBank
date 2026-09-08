package br.com.cyberbank.categoria.persistencia;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface CategoriaJpa extends JpaRepository<CategoriaEntity, Long> {

    List<CategoriaEntity> findByAmbienteId(Long ambienteId);
}

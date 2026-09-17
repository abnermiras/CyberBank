package br.com.cyberbank.categoria.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.categoria.dominio.OperacaoDeSistema;
import br.com.cyberbank.categoria.dominio.Sentido;

import org.springframework.data.jpa.repository.JpaRepository;

interface CategoriaJpa extends JpaRepository<CategoriaEntity, Long> {

    List<CategoriaEntity> findByAmbienteId(Long ambienteId);

    Optional<CategoriaEntity> findByIdAndAmbienteId(Long id, Long ambienteId);

    Optional<CategoriaEntity> findByAmbienteIdAndOperacaoAndSentido(
            Long ambienteId, OperacaoDeSistema operacao, Sentido sentido);

    boolean existsByPaiIdAndAmbienteId(Long paiId, Long ambienteId);

    void deleteByIdAndAmbienteId(Long id, Long ambienteId);
}

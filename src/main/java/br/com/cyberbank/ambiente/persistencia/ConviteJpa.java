package br.com.cyberbank.ambiente.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.ambiente.dominio.SituacaoDoConvite;

import org.springframework.data.jpa.repository.JpaRepository;

interface ConviteJpa extends JpaRepository<ConviteEntity, Long> {

    Optional<ConviteEntity> findByAmbienteIdAndEmailAndSituacao(
            Long ambienteId, String email, SituacaoDoConvite situacao);

    List<ConviteEntity> findByAmbienteIdAndSituacaoOrderByCriadoEmAscIdAsc(
            Long ambienteId, SituacaoDoConvite situacao);

    List<ConviteEntity> findByEmailAndSituacaoOrderByCriadoEmAscIdAsc(
            String email, SituacaoDoConvite situacao);
}

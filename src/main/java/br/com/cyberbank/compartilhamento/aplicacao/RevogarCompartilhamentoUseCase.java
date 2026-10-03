package br.com.cyberbank.compartilhamento.aplicacao;

import java.time.Clock;

import br.com.cyberbank.ambiente.dominio.Acesso;
import br.com.cyberbank.ambiente.dominio.AcessoRepository;
import br.com.cyberbank.ambiente.dominio.Ambiente;
import br.com.cyberbank.ambiente.dominio.AmbienteRepository;
import br.com.cyberbank.compartilhamento.dominio.Compartilhamento;
import br.com.cyberbank.compartilhamento.dominio.CompartilhamentoRepository;
import br.com.cyberbank.comum.erro.CodigoDeErro;
import br.com.cyberbank.comum.erro.RegraDeDominioException;
import br.com.cyberbank.comum.tempo.DiaLocal;
import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.evento.dominio.Alvo;
import br.com.cyberbank.evento.dominio.Evento;
import br.com.cyberbank.evento.dominio.EventoRepository;
import br.com.cyberbank.evento.dominio.TipoDeEvento;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RevogarCompartilhamentoUseCase {

    private final CompartilhamentoRepository compartilhamentos;
    private final ContaRepository contas;
    private final AcessoRepository acessos;
    private final AmbienteRepository ambientes;
    private final EventoRepository eventos;
    private final DiaLocal diaLocal;
    private final Clock relogio;

    public RevogarCompartilhamentoUseCase(CompartilhamentoRepository compartilhamentos,
            ContaRepository contas, AcessoRepository acessos, AmbienteRepository ambientes,
            EventoRepository eventos, DiaLocal diaLocal, Clock relogio) {
        this.compartilhamentos = compartilhamentos;
        this.contas = contas;
        this.acessos = acessos;
        this.ambientes = ambientes;
        this.eventos = eventos;
        this.diaLocal = diaLocal;
        this.relogio = relogio;
    }

    @Transactional
    public void executar(Long usuarioId, Long ambienteId, Long contaId, Long ambienteDestinoId) {
        Acesso acesso = acessos.buscar(usuarioId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
        Compartilhamento vinculo = compartilhamentos.buscar(contaId, ambienteDestinoId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        vinculo.exigirRevogavelPor(ambienteId, acesso.papel().podeCompartilhar());
        Conta conta = contas.buscarAcessivel(contaId, ambienteId)
                .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));

        registrar(vinculo, conta, ambienteId, usuarioId);
        compartilhamentos.revogar(contaId, ambienteDestinoId);
    }

    @Transactional
    public void devolverOsEmprestadosPor(Long emprestadorId, Long ambienteDestinoId,
            Long quemAgiuId) {
        for (Compartilhamento vinculo : compartilhamentos.listarRecebidos(ambienteDestinoId)) {
            if (!vinculo.emprestadoPor(emprestadorId)) {
                continue;
            }
            Conta conta = contas.buscarAcessivel(vinculo.contaId(), ambienteDestinoId)
                    .orElseThrow(() -> new RegraDeDominioException(CodigoDeErro.NAO_ENCONTRADO));
            registrar(vinculo, conta, ambienteDestinoId, quemAgiuId);
            compartilhamentos.revogar(vinculo.contaId(), ambienteDestinoId);
        }
    }

    private void registrar(Compartilhamento vinculo, Conta conta, Long ambienteId,
            Long autorId) {
        boolean devolvida = vinculo.devolvidoPor(ambienteId);
        eventos.registrar(Evento.doUsuario(ambienteId, autorId, TipoDeEvento.VINCULO_REVOGADO,
                Alvo.conta(conta.id()),
                Evento.dados("conta", conta.nome(),
                        "destinoId", vinculo.ambienteDestinoId(),
                        "destino", nome(vinculo.ambienteDestinoId()),
                        "origem", devolvida ? nome(vinculo.ambienteOrigemId()) : null,
                        "devolvida", devolvida ? true : null),
                diaLocal.hoje(), relogio.instant()));
    }

    private String nome(Long ambienteId) {
        return ambientes.buscar(ambienteId).map(Ambiente::nome).orElse(null);
    }
}

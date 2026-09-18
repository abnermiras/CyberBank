package br.com.cyberbank.conta.persistencia;

import java.util.List;
import java.util.Optional;

import br.com.cyberbank.conta.dominio.Conta;
import br.com.cyberbank.conta.dominio.ContaRepository;
import br.com.cyberbank.conta.dominio.ContratoDeCartao;

import org.springframework.stereotype.Repository;

@Repository
public class ContaRepositoryJpa implements ContaRepository {

    private final ContaJpa jpa;

    ContaRepositoryJpa(ContaJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Conta salvar(Conta conta) {
        return paraDominio(jpa.save(paraEntidade(conta)));
    }

    @Override
    public Optional<Conta> buscarDoAmbiente(Long id, Long ambienteId) {
        return jpa.findByIdAndAmbienteId(id, ambienteId).map(ContaRepositoryJpa::paraDominio);
    }

    @Override
    public List<Conta> listarDoAmbiente(Long ambienteId) {
        return jpa.findByAmbienteIdOrderByNomeAsc(ambienteId).stream()
                .map(ContaRepositoryJpa::paraDominio)
                .toList();
    }

    @Override
    public void excluir(Long id, Long ambienteId) {
        jpa.deleteByIdAndAmbienteId(id, ambienteId);
    }

    private static ContaEntity paraEntidade(Conta c) {
        ContratoDeCartao contrato = c.contrato();
        return new ContaEntity(c.id(), c.ambienteId(), c.nome(), c.tipo(),
                c.entraNoFluxoDeCaixa(), c.entraEmCaixa(),
                contrato == null ? null : contrato.limiteCentavos(),
                contrato == null ? null : contrato.limiteInformadoEm(),
                contrato == null ? null : (short) contrato.diaVencimento(),
                contrato == null ? null : (short) contrato.diasAntesFechamento(),
                contrato == null ? null : contrato.contaPagadoraPadraoId(),
                c.inativa(), c.criadaEm());
    }

    private static Conta paraDominio(ContaEntity e) {
        ContratoDeCartao contrato = e.getDiaVencimento() == null ? null
                : new ContratoDeCartao(e.getLimiteCentavos(), e.getLimiteInformadoEm(),
                        e.getDiaVencimento().intValue(), e.getDiasAntesFechamento().intValue(),
                        e.getContaPagadoraPadraoId());

        return new Conta(e.getId(), e.getAmbienteId(), e.getNome(), e.getTipo(),
                e.isEntraNoFluxoDeCaixa(), e.isEntraEmCaixa(), contrato, e.isInativa(),
                e.getCriadaEm());
    }
}

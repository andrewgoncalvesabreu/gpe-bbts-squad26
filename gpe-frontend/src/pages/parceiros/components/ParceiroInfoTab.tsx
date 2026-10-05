import { PORTE_OPTIONS, TIPO_ORGANIZACAO_OPTIONS, labelFrom } from '../../../constants/parceiro'
import type { Parceiro } from '../../../types/parceiro'
import { formatDate } from '../../../utils/format'
import { maskCnpj } from '../../../utils/format'
import { parseResponsavel } from '../../../utils/parceiroMapper'

function Row({ label, value }: { label: string; value?: string | null }) {
  return (
    <div className="py-1.5">
      <dt className="text-xs uppercase tracking-wide text-zinc-400">{label}</dt>
      <dd className="text-sm text-zinc-900">{value || '—'}</dd>
    </div>
  )
}

function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="rounded-xl border border-zinc-200 bg-white p-5">
      <h3 className="mb-2 text-base font-semibold">{title}</h3>
      <dl>{children}</dl>
    </section>
  )
}

export function ParceiroInfoTab({ parceiro }: { parceiro: Parceiro }) {
  const resp = parseResponsavel(parceiro.responsaveis)
  const areas = parceiro.segmentoAtuacao?.split(',').map((a) => a.trim()).filter(Boolean) ?? []

  return (
    <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
      <div className="flex flex-col gap-4 lg:col-span-2">
        <Card title="Dados Institucionais">
          <Row label="Razão Social" value={parceiro.razaoSocial} />
          <Row label="Nome Fantasia" value={parceiro.nomeFantasia} />
          <Row label="CNPJ" value={maskCnpj(parceiro.cnpj)} />
          <Row label="Tipo de Instituição" value={labelFrom(TIPO_ORGANIZACAO_OPTIONS, parceiro.tipoOrganizacao)} />
          <Row label="Porte" value={labelFrom(PORTE_OPTIONS, parceiro.porte)} />
          <Row label="Endereço" value={parceiro.enderecoCompleto} />
        </Card>

        <section className="rounded-xl border border-zinc-200 bg-white p-5">
          <h3 className="mb-3 text-base font-semibold">Áreas de Atuação</h3>
          {areas.length === 0 ? (
            <p className="text-sm text-zinc-500">Nenhuma área informada.</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {areas.map((a) => (
                <span key={a} className="rounded-full bg-zinc-100 px-3 py-1 text-sm text-zinc-700">
                  {a}
                </span>
              ))}
            </div>
          )}
        </section>
      </div>

      <div className="flex flex-col gap-4">
        <Card title="Contato Principal">
          <Row label="Nome" value={resp.nome} />
          <Row label="Cargo" value={resp.cargo} />
          <Row label="E-mail" value={parceiro.emailContato} />
          <Row label="Telefone" value={parceiro.telefone} />
        </Card>
        <Card title="Cadastro">
          <Row label="Data de cadastro" value={formatDate(parceiro.dataCadastro)} />
        </Card>
      </div>
    </div>
  )
}

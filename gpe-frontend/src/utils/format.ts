export const onlyDigits = (v: string) => v.replace(/\D/g, '')

export function maskCnpj(value: string): string {
  const cnpj = value
    .toUpperCase()
    .replace(/[^0-9A-Z]/g, '')
    .slice(0, 14)

  let result = ''

  if (cnpj.length > 0) {
    result += cnpj.slice(0, 2)
  }

  if (cnpj.length > 2) {
    result += '.' + cnpj.slice(2, 5)
  }

  if (cnpj.length > 5) {
    result += '.' + cnpj.slice(5, 8)
  }

  if (cnpj.length > 8) {
    result += '/' + cnpj.slice(8, 12)
  }

  if (cnpj.length > 12) {
    result += '-' + cnpj.slice(12, 14)
  }

  return result
}

export function maskCep(value: string): string {
  return onlyDigits(value).slice(0, 8).replace(/^(\d{5})(\d)/, '$1-$2')
}

export function maskTelefone(value: string): string {
  const d = onlyDigits(value).slice(0, 11)
  if (d.length <= 10) {
    return d.replace(/^(\d{2})(\d)/, '($1) $2').replace(/(\d{4})(\d)/, '$1-$2')
  }
  return d.replace(/^(\d{2})(\d)/, '($1) $2').replace(/(\d{5})(\d)/, '$1-$2')
}

export function formatDateTime(iso?: string | null): string {
  if (!iso) return '—'
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}

export function formatDate(iso?: string | null): string {
  if (!iso) return '—'
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleDateString('pt-BR')
}

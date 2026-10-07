
export function isValidCnpj(value: string): boolean {
  // Remove pontuação, mantém letras e números
  const cnpj = value
    .toUpperCase()
    .replace(/[^0-9A-Z]/g, '')

  // CNPJ precisa ter exatamente 14 caracteres
  if (cnpj.length !== 14) return false

  // Não aceita caracteres repetidos
  if (/^([0-9A-Z])\1+$/.test(cnpj)) return false

  // Os 2 últimos caracteres são obrigatoriamente numéricos
  if (!/^[0-9A-Z]{12}\d{2}$/.test(cnpj)) return false

  // Converte o caractere para o valor usado no cálculo:
  // 0-9 -> 0-9
  // A -> 17
  // B -> 18
  // ...
  // Z -> 42
  const charValue = (char: string): number => {
    return char.charCodeAt(0) - 48
  }

  const calculateDigit = (
    base: string,
    weights: number[],
  ): number => {
    const sum = base
      .split('')
      .reduce(
        (acc, char, index) =>
          acc + charValue(char) * weights[index],
        0,
      )

    const remainder = sum % 11

    return remainder < 2 ? 0 : 11 - remainder
  }

  // Primeiro dígito verificador
  const digit1 = calculateDigit(
    cnpj.slice(0, 12),
    [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2],
  )

  // Segundo dígito verificador
  const digit2 = calculateDigit(
    cnpj.slice(0, 13),
    [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2],
  )

  return (
    Number(cnpj[12]) === digit1 &&
    Number(cnpj[13]) === digit2
  )
}

export const isValidEmail = (v: string) =>
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)
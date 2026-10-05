import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="py-20 text-center">
      <h1 className="text-2xl font-bold">Página não encontrada</h1>
      <p className="mt-2 text-zinc-500">O endereço acessado não existe ou o módulo ainda não foi implementado.</p>
      <Link to="/parceiros" className="mt-4 inline-block text-zinc-800 underline">
        Ir para Parceiros
      </Link>
    </div>
  )
}

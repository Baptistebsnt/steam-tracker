import { useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '@/lib/auth'

function SteamCallback() {
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const { loginWithSteam } = useAuth()
  const handled = useRef(false)

  useEffect(() => {
    if (handled.current) return
    handled.current = true

    const token = params.get('token')
    const email = params.get('email')
    const error = params.get('error')

    if (token && email) {
      loginWithSteam({
        token,
        email,
        steamId: params.get('steamId'),
        displayName: params.get('displayName') ?? email,
        avatarUrl: params.get('avatarUrl'),
      })
      navigate('/dashboard', { replace: true })
    } else {
      navigate(`/login?error=${error ?? 'steam_auth_failed'}`, { replace: true })
    }
  }, [params, loginWithSteam, navigate])

  return null
}

export default SteamCallback

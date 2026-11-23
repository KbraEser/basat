// Same rules as @Size and @Pattern on RegisterRequest.password in auth-service, so the form and the API agree.
// \p{Lu} / \p{Ll} are any upper / lower-case letter, Turkish ones (Ş, ğ) included.
export const passwordRules = [
  { label: 'En az 8 karakter', test: (value: string) => value.length >= 8 },
  { label: 'Bir büyük harf', test: (value: string) => /\p{Lu}/u.test(value) },
  { label: 'Bir küçük harf', test: (value: string) => /\p{Ll}/u.test(value) },
  { label: 'Bir rakam', test: (value: string) => /\d/.test(value) },
]

export const isStrongPassword = (value: string) => passwordRules.every((rule) => rule.test(value))

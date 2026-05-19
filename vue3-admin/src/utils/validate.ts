/**
 * Form validation rules
 */

export const validateEmail = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!emailRegex.test(value)) {
    callback(new Error('Please enter a valid email address'))
  } else {
    callback()
  }
}

export const validateMobile = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  const mobileRegex = /^1[3-9]\d{9}$/
  if (!mobileRegex.test(value)) {
    callback(new Error('Please enter a valid mobile number'))
  } else {
    callback()
  }
}

export const validateUsername = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('Username is required'))
    return
  }
  const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/
  if (!usernameRegex.test(value)) {
    callback(new Error('Username must be 4-20 characters, alphanumeric and underscore only'))
  } else {
    callback()
  }
}

export const validatePassword = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  if (value.length < 6) {
    callback(new Error('Password must be at least 6 characters'))
    return
  }
  if (value.length > 20) {
    callback(new Error('Password must not exceed 20 characters'))
    return
  }
  callback()
}

export const validateRoleCode = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('Role code is required'))
    return
  }
  const codeRegex = /^[A-Z_]{2,30}$/
  if (!codeRegex.test(value)) {
    callback(new Error('Role code must be 2-30 uppercase letters and underscore only'))
  } else {
    callback()
  }
}

export const validateIP = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  const ipRegex = /^(\d{1,3}\.){3}\d{1,3}$/
  if (!ipRegex.test(value)) {
    callback(new Error('Please enter a valid IP address'))
    return
  }
  const parts = value.split('.')
  for (const part of parts) {
    const num = parseInt(part, 10)
    if (num < 0 || num > 255) {
      callback(new Error('IP address octets must be between 0 and 255'))
      return
    }
  }
  callback()
}

export const validatePort = (rule: any, value: number, callback: any) => {
  if (!value && value !== 0) {
    callback()
    return
  }
  if (value < 1 || value > 65535) {
    callback(new Error('Port must be between 1 and 65535'))
  } else {
    callback()
  }
}

export const validateUrl = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  try {
    new URL(value)
    callback()
  } catch {
    callback(new Error('Please enter a valid URL'))
  }
}

export const validatePasswordStrength = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  if (value.length < 8) {
    callback(new Error('Password must be at least 8 characters'))
    return
  }
  const hasUpper = /[A-Z]/.test(value)
  const hasLower = /[a-z]/.test(value)
  const hasNumber = /\d/.test(value)
  const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(value)
  const strength = [hasUpper, hasLower, hasNumber, hasSpecial].filter(Boolean).length
  if (strength < 3) {
    callback(new Error('Password must contain at least 3 of: uppercase, lowercase, number, special character'))
    return
  }
  callback()
}

export const validateNickname = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  if (value.length < 2 || value.length > 30) {
    callback(new Error('Nickname must be 2-30 characters'))
    return
  }
  const nicknameRegex = /^[一-龥a-zA-Z0-9_\-\s]+$/
  if (!nicknameRegex.test(value)) {
    callback(new Error('Nickname can contain Chinese, letters, numbers, underscore and hyphen'))
  } else {
    callback()
  }
}

export const validateIdCard = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  const idCardRegex = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/
  if (!idCardRegex.test(value)) {
    callback(new Error('Please enter a valid ID card number'))
  } else {
    callback()
  }
}

export const validatePostCode = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  const postCodeRegex = /^\d{6}$/
  if (!postCodeRegex.test(value)) {
    callback(new Error('Please enter a valid 6-digit post code'))
  } else {
    callback()
  }
}

export const validateQQ = (rule: any, value: string, callback: any) => {
  if (!value) {
    callback()
    return
  }
  const qqRegex = /^[1-9]\d{4,10}$/
  if (!qqRegex.test(value)) {
    callback(new Error('Please enter a valid QQ number'))
  } else {
    callback()
  }
}

/**
 * Common validation rules for reuse
 */
export const commonRules = {
  email: [
    { required: false, validator: validateEmail, trigger: 'blur' }
  ],
  mobile: [
    { required: false, validator: validateMobile, trigger: 'blur' }
  ],
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' }
  ],
  password: [
    { required: false, validator: validatePassword, trigger: 'blur' }
  ],
  passwordStrength: [
    { required: false, validator: validatePasswordStrength, trigger: 'blur' }
  ],
  nickname: [
    { required: false, validator: validateNickname, trigger: 'blur' }
  ],
  idCard: [
    { required: false, validator: validateIdCard, trigger: 'blur' }
  ],
  postCode: [
    { required: false, validator: validatePostCode, trigger: 'blur' }
  ],
  qq: [
    { required: false, validator: validateQQ, trigger: 'blur' }
  ],
  ip: [
    { required: false, validator: validateIP, trigger: 'blur' }
  ],
  url: [
    { required: false, validator: validateUrl, trigger: 'blur' }
  ],
  port: [
    { required: false, validator: validatePort, trigger: 'blur' }
  ],
  roleCode: [
    { required: true, validator: validateRoleCode, trigger: 'blur' }
  ],
  required: [
    { required: true, message: 'This field is required', trigger: 'blur' }
  ]
}

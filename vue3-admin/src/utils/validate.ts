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
  required: [
    { required: true, message: 'This field is required', trigger: 'blur' }
  ]
}

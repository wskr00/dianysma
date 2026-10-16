package domain

type EducationType string

const (
	Degree        EducationType = "DEGREE"
	Course        EducationType = "COURSE"
	Certification EducationType = "CERTIFICATION"
)

type LanguageProficiency string

const (
	Basic        LanguageProficiency = "BASIC"
	Intermediate LanguageProficiency = "INTERMEDIATE"
	Advanced     LanguageProficiency = "ADVANCED"
	Fluent       LanguageProficiency = "FLUENT"
	Native       LanguageProficiency = "NATIVE"
)

package result

import (
	"time"

	"github.com/wskr00/dianysma/src/knowledge/domain"
)

type EducationDetails struct {
	ID            string               `json:"id" jsonschema:"UUID da formação." format:"uuid"`
	Institution   string               `json:"institution" jsonschema:"Nome da instituição de ensino ou certificadora."`
	Type          domain.EducationType `json:"type" jsonschema:"Tipo da formação: CERTIFICATION, COURSE ou DEGREE."`
	Title         string               `json:"title" jsonschema:"Nome do curso, certificação ou formação."`
	Degree        *string              `json:"degree" jsonschema:"Grau ou título acadêmico, quando informado."`
	Description   *string              `json:"description" jsonschema:"Descrição da formação, quando informada."`
	WorkloadHours *int                 `json:"workloadHours" jsonschema:"Carga horária total em horas, quando informada."`
	StartedAt     *string              `json:"startedAt" jsonschema:"Data de início no formato AAAA-MM-DD, quando informada." format:"date"`
	CompletedAt   *string              `json:"completedAt" jsonschema:"Data efetiva ou prevista de conclusão no formato AAAA-MM-DD, quando informada." format:"date"`
	Expected      *bool                `json:"expected" jsonschema:"true se completedAt for uma previsão; false se for a data efetiva; null se não houver essa informação."`
	CreatedAt     time.Time            `json:"createdAt" jsonschema:"Data e hora de criação, com fuso horário."`
	UpdatedAt     time.Time            `json:"updatedAt" jsonschema:"Data e hora da última atualização, com fuso horário."`
}

type EducationList struct {
	Educations []EducationDetails `json:"educations" jsonschema:"Formações encontradas, ordenadas pela atualização mais recente. A lista pode estar vazia."`
}
